package decisiontree.util;

import org.bouncycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.Arrays;

public class WrappedByteArray implements Serializable {
    public final byte[] array;

    public WrappedByteArray(byte[] array) {
        this.array = array;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WrappedByteArray && Arrays.equals(array, ((WrappedByteArray) obj).array);
    }

    @Override
    public String toString() {
        return Hex.toHexString(array);
    }
}
