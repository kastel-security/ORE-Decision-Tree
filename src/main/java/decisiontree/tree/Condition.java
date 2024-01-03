package decisiontree.tree;

import decisiontree.data.Data;

import java.io.Serializable;
import java.util.Objects;

public class Condition implements Serializable {

    public final int attribute;
    public final Comparable split;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condition condition = (Condition) o;
        return attribute == condition.attribute && split.equals(condition.split);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, split);
    }

    public Condition(int attribute, Comparable split) {
        this.attribute = attribute;
        this.split = split;
    }

    public  boolean test(Data data) {
        var attr = data.getAttribute(attribute);
        var cmp = attr.compareTo(split);
        return cmp <= 0;
    }

    @Override
    public String toString() {
        return "<condition attribute=\"" + attribute + "\" split=\"" + split + "\" />";
    }
}
