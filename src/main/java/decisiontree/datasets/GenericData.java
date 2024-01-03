package decisiontree.datasets;

import decisiontree.data.Data;

import java.util.Arrays;

public class GenericData implements Data {
    private final Comparable[] attributes;

    public GenericData(Comparable[] comp) {
        this.attributes = comp;
    }

    @Override
    public Comparable getAttribute(int attribute) {
        return attributes[attribute];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(attributes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GenericData && Arrays.equals(attributes, ((GenericData)obj).attributes);
    }
}
