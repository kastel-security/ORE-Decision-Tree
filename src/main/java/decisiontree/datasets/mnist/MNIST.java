package decisiontree.datasets.mnist;

import decisiontree.data.Data;
import decisiontree.data.Dataset;
import decisiontree.datasets.DatasetSource;
import kotlin.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public enum MNIST implements DatasetSource<Integer> {
    MNIST("http://yann.lecun.com/exdb/mnist/", "train"),
    MNIST_TEST("http://yann.lecun.com/exdb/mnist/", "t10k"),
    FASHION("http://fashion-mnist.s3-website.eu-central-1.amazonaws.com/", "train"),
    FASHION_TEST("http://fashion-mnist.s3-website.eu-central-1.amazonaws.com/", "t10k"),
    KMNIST("http://codh.rois.ac.jp/kmnist/dataset/kmnist/", "train"),
    KMNIST_TEST("http://codh.rois.ac.jp/kmnist/dataset/kmnist/", "t10k"),
    EMNIST_BALANCED("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-balanced-", "train"),
    EMNIST_BALANCED_TEST("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-balanced-", "test"),
    EMNIST_BYCLASS("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-byclass-", "train"),
    EMNIST_BYCLASS_TEST("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-byclass-", "test"),
    EMNIST_BYMERGE("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-bymerge-", "train"),
    EMNIST_BYMERGE_TEST("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-bymerge-", "test"),
    EMNIST_DIGITS("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-digits-", "train"),
    EMNIST_DIGITS_TEST("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-digits-", "test"),
    EMNIST_LETTERS("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-letters-", "train"),
    EMNIST_LETTERS_TEST("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-letters-", "test"),
    EMNIST_MNIST("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-mnist-", "train"),
    EMNIST_MNIST_TEST("https://github.com/aurelienduarte/emnist/blob/master/gzip/emnist-mnist-", "test");

    private final String remote;
    private final String name;

    private MNIST(String remote, String name) {
        this.remote = remote;
        this.name = name;
    }

    private boolean shouldTranspose() {
        return this.toString().startsWith("EMNIST");
    }

    public boolean isTrainingSet() {
        return (ordinal() & 1) == 0;
    }

    public boolean isTestSet() {
        return !isTrainingSet();
    }

    public decisiontree.datasets.mnist.MNIST getOther() {
        return values()[ordinal() ^ 1];
    }

    public decisiontree.datasets.mnist.MNIST getTrainingSet() {
        return isTrainingSet() ? this : getOther();
    }

    public decisiontree.datasets.mnist.MNIST getTestSet() {
        return getTrainingSet().getOther();
    }

    public String getDatasetName() {
        var name = toString();
        if (isTestSet()) {
            name = name.substring(0, name.length() - "_TEST".length());
        }
        return name.toLowerCase();
    }

    private InputStream loadDatasetFile(String name) throws IOException {
        name = this.name + name + "-ubyte.gz";
        return new GZIPInputStream(DatasetSource.loadFileCached(remote + name + "?raw=true", getDatasetName() + "/" + name));
    }

    private static int readInt(InputStream is) throws IOException {
        var ret = 0;
        for (int i = 0; i < 4; i++) {
            ret <<= 8;
            ret |= is.read();
        }
        return ret;
    }

    private static int readUByte(InputStream is) throws IOException {
        return is.read();
    }

    private List<Integer> loadLabels(InputStream is, int skip, int max) throws IOException {
        var magic = readInt(is);
        assert magic == 2049;
        var amount = readInt(is);
        if (max < amount) {
            amount = max;
        }
        var ret = new ArrayList<Integer>(amount);
        for (int i = 0; i < amount; i++) {
            var current = readUByte(is);
            if (i >= skip) {
                ret.add(current);
            }
        }
        return ret;
    }

    public Pair<Integer, Integer> getImageDimensions() {
        try (var is = getImageInputStream()) {
            readInt(is);
            readInt(is);
            var rows = readInt(is);
            var columns = readInt(is);
            return new Pair<>(rows, columns);
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair<>(0, 0);
        }
    }

    private Pair<List<Data>, Integer> loadImages(InputStream is, int skip, int max) throws IOException {
        var magic = readInt(is);
        assert magic == 2051;
        var amount = readInt(is);
        if (max < amount) {
            amount = max;
        }
        var rows = readInt(is);
        var columns = readInt(is);
        assert rows == columns;
        var ret1 = new ArrayList<Data>(amount);
        var transpose = shouldTranspose();
        for (int i = 0; i < amount; i++) {
            byte[] data = new byte[rows * columns];
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    byte current = (byte) readUByte(is);
                    int idx;
                    if (transpose) {
                        idx = y * columns + x;
                    } else {
                        idx = x * rows + y;
                    }
                    data[idx] = current;
                }
            }
            if (skip-- <= 0) {
                ret1.add(new MNISTImage(data));
            }
        }
        return new Pair<>(ret1, rows * columns);
    }

    private InputStream getLabelInputStream() throws IOException {
        return loadDatasetFile("-labels-idx1");
    }

    private InputStream getImageInputStream() throws IOException {
        return loadDatasetFile("-images-idx3");
    }

    @Override
    public int getDataSize() {
        try (var is = getLabelInputStream()) {
            var magic = readInt(is);
            assert magic == 2049;
            return readInt(is);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int getAttributeBitSize() {
        return 8;
    }

    @Override
    public Dataset<Integer> loadDataset(int skip, int max) {
        Pair<List<Data>, Integer> data;
        List<Integer> labels;
        try (var dataIS = getImageInputStream()) {
            try (var labelIS = getLabelInputStream()) {
                data = loadImages(dataIS, skip, max);
                labels = loadLabels(labelIS, skip, max);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        var realData = data.getFirst();
        var features = data.getSecond();

        assert realData.size() == labels.size();
        var tuples = new ArrayList<Pair<Data, Integer>>(realData.size());
        for (int i = 0; i < realData.size(); i++) {
            tuples.add(new Pair<>(realData.get(i), labels.get(i)));
        }

        return new Dataset<>(tuples, features);
    }

    private void exportImage(MNISTImage data, File file) {
        var pixels = data.pixels;
        var dimensions = getImageDimensions();
        var width = dimensions.getFirst();
        var height = dimensions.getSecond();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                out.setRGB(x, y, pixels[x * height + y] << 8);
            }
        }
        try {
            ImageIO.write(out, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportSample(Dataset<Integer> data) {
        var processedSet = new HashSet<Integer>();
        var elements = data.getAllData();
        for (var element : elements) {
            var label = element.getSecond();
            if (processedSet.contains(label)) {
                continue;
            }
            var targetFile = new File("./" + label + ".png");
            processedSet.add(label);
            exportImage((MNISTImage) element.getFirst(), targetFile);
        }
    }

    public DatasetSource<Integer> getTrimmed(Function<MNISTImage, MNISTImage> trimmer) {
        return new DatasetSource<>() {
            @Override
            public Dataset<Integer> loadDataset(int skip, int max) {
                var dataset = MNIST.this.loadDataset(skip, max);
                var nAttributes = new int[] { -1 };
                var newData = dataset.getAllData().stream().map((data) -> {
                    var datapoint = data.getFirst();
                    var label = data.getSecond();
                    var newImage = trimmer.apply((MNISTImage) datapoint);
                    nAttributes[0] = newImage.getNAttributes();
                    return new Pair<Data, Integer>(newImage, label);
                }).collect(Collectors.toList());
                return new Dataset<>(newData, nAttributes[0]);
            }

            @Override
            public String getDatasetName() {
                return MNIST.this.getDatasetName();
            }

            @Override
            public boolean isTestSet() {
                return MNIST.this.isTestSet();
            }

            @Override
            public DatasetSource<Integer> getOther() {
                return MNIST.this.getOther().getTrimmed(trimmer);
            }

            @Override
            public int getDataSize() {
                return MNIST.this.getDataSize();
            }

            @Override
            public int getAttributeBitSize() {
                return MNIST.this.getAttributeBitSize();
            }
        };
    }
}
