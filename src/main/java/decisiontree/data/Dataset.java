package decisiontree.data;

import decisiontree.tree.Condition;
import decisiontree.util.Counter;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Dataset<L> {

    private final List<Pair<Data, L>> data;
    private final int nAttributes;

    public Dataset(List<Pair<Data, L>> data, int nAttributes) {
        this.data = data;
        this.nAttributes = nAttributes;
    }

    public int getNAttributes() {
        return nAttributes;
    }

    public List<Pair<Data, L>> getAllData() {
        return data;
    }

    public Pair<Dataset<L>, Dataset<L>> splitAt(Condition condition) {
        List<Pair<Data, L>> firstData = new ArrayList<>(nAttributes / 2);
        List<Pair<Data, L>> secondData = new ArrayList<>(nAttributes / 2);
        for (var t : data) {
            var dat = t.getFirst();
            var target = condition.test(dat) ? firstData : secondData;
            target.add(t);
        }
        return new Pair<>(new Dataset<>(firstData, nAttributes), new Dataset<>(secondData, nAttributes));
    }

    public boolean isRealSplit(Condition condition) {
        var hasTrue = false;
        var hasFalse = false;
        for (var entry : data) {
            if (condition.test(entry.component1())) {
                if (hasFalse) {
                    return true;
                }
                hasTrue = true;
            } else {
                if (hasTrue) {
                    return true;
                }
                hasFalse = true;
            }
        }
        return false;
    }

    public int getDataSize() {
        return data.size();
    }

    public Dataset<L> getSubset(int begin, int end) {
        var newList = data.stream().skip(begin).limit(end - begin).collect(Collectors.toList());
        return new Dataset<>(newList, nAttributes);
    }

    @Override
    public String toString() {
        return "Dataset (" + getDataSize() + " elements, " + nAttributes + "attributes)";
    }

    public Counter<L> getLabelCounter() {
        var ret = new Counter<L>();
        for (var element : data) {
            ret.add(element.component2());
        }
        return ret;
    }
}
