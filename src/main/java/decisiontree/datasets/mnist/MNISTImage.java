package decisiontree.datasets.mnist;

import decisiontree.data.Data;

public class MNISTImage implements Data {
    final byte[] pixels;

    public MNISTImage(byte[] data) {
        this.pixels = data;
    }

    public Integer getAttribute(int attr) {
        return pixels[attr] & 0xFF;
    }

    public int getNAttributes() {
        return pixels.length;
    }

    public MNISTImage trim(int border) {
        int oldEdgeLength = (int) Math.sqrt(pixels.length);
        assert oldEdgeLength * oldEdgeLength == pixels.length;
        int newEdgeLength = oldEdgeLength - 2 * border;
        byte[] ret = new byte[newEdgeLength * newEdgeLength];
        for (int i = 0; i < newEdgeLength; i++) {
            for (int j = 0; j < newEdgeLength; j++) {
                ret[j + newEdgeLength * i] = pixels[border + j + (border + i) * oldEdgeLength];
            }
        }
        return new MNISTImage(ret);
    }

    public MNISTImage downscale(int scalingFactor) {
        int oldEdgeLength = (int) Math.sqrt(pixels.length);
        assert oldEdgeLength * oldEdgeLength == pixels.length;
        assert oldEdgeLength % scalingFactor == 0;
        int newEdgeLength = oldEdgeLength / scalingFactor;
        byte[] ret = new byte[newEdgeLength * newEdgeLength];
        int scaleSq = scalingFactor * scalingFactor;

        for (int i = 0; i < newEdgeLength; i++) {
            for (int j = 0; j < newEdgeLength; j++) {
                int acc = 0;
                for (int iD = 0; iD < scalingFactor; iD++) {
                    for (int jD = 0; jD < scalingFactor; jD++) {
                        acc += pixels[j * scalingFactor + jD + (i * scalingFactor + iD) * oldEdgeLength] & 0xFF;
                    }
                }
                ret[j + i * newEdgeLength] = (byte) (acc / scaleSq);
            }
        }
        return new MNISTImage(ret);
    }
}
