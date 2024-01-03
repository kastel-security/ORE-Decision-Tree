package decisiontree.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Counter<T> {
    private Map<T, Integer> map = new HashMap<>();

    public int getCount(Object ele) {
        var count = map.get(ele);
        if (count != null) {
            return count;
        } else {
            return 0;
        }
    }

    private void setCount(T ele, int amount) {
        if (amount == 0) {
            map.remove(ele);
        } else if (amount < 0) {
            throw new IllegalArgumentException();
        } else {
            map.put(ele, amount);
        }
    }

    public void add(T ele) {
        setCount(ele, getCount(ele) + 1);
    }

    public void remove(T ele) {
        var prev = getCount(ele);
        if (prev <= 0) {
            throw new IllegalStateException("Cannot remove from counter, when it does not exist.");
        }
        setCount(ele, prev - 1);
    }

    public int getTotalCount() {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Collection<T> getAllKeys() {
        return map.keySet();
    }

    public T getMax() {
        var max = map.keySet().iterator().next();
        for (var ele : map.keySet()) {
            if (getCount(ele) > getCount(max)) {
                max = ele;
            }
        }
        return max;
    }

    public int getMaxCount() {
        return map.values().stream().mapToInt(Integer::intValue).max().getAsInt();
    }
}
