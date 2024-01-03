package decisiontree.datasets;

import decisiontree.data.Dataset;

import java.io.*;
import java.net.URL;

public interface DatasetSource<T> {

    static final String DATASET_BASE_DIRECTORY = "./datasets/";

    default Dataset<T> loadDataset() {
        return loadDataset(0, Integer.MAX_VALUE);
    }
    default Dataset<T> loadDataset(int max) {
        return loadDataset(0, max);
    }
    Dataset<T> loadDataset(int skip, int max);

    String getDatasetName();

    default DatasetSource<T> getTestSet() {
        if (isTestSet()) {
            return this;
        } else {
            return getOther();
        }
    }

    boolean isTestSet();

    default boolean isTrainingSet() {
        return !isTestSet();
    }

    default DatasetSource<T> getTrainingSet() {
        if (isTrainingSet()) {
            return this;
        } else {
            return getOther();
        }
    }

    DatasetSource<T> getOther();

    public static InputStream loadFileCached(String urlString, String filename) throws IOException {
        var local = new File(DATASET_BASE_DIRECTORY + "/" + filename);
        if (!local.exists()) {
            local.getParentFile().mkdirs();
            var url = new URL(urlString);
            try (var os = new BufferedOutputStream(new FileOutputStream(local))) {
                try (var is = new BufferedInputStream(url.openConnection().getInputStream())) {
                    is.transferTo(os);
                }
            }
        }
        return new BufferedInputStream(new FileInputStream(local));
    }

    default int getDataSize() {
        return loadDataset().getDataSize();
    }

    int getAttributeBitSize();
}
