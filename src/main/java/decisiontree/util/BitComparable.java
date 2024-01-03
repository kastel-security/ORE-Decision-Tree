package decisiontree.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public interface BitComparable<T extends BitComparable<T>> extends Comparable<T> {

    int getBitCount();
    default boolean areBitsEqual(T other, int position) {
        return compareBits(other, position) == 0;
    }
    int compareBits(T other, int position);

    static <T, V extends BitComparable<V>> void msdRadixSortStableBy(List<T> list, Function<T, V> compareBy) {
        var array = list.toArray();
        msdRadixSortStableBy((T[])array, compareBy, 0, array.length, 0, (T[])new Object[array.length - 1], (T[])new Object[array.length - 1]);
        list.clear();
        list.addAll((List<T>) Arrays.asList(array));
    }

    static <T extends BitComparable<T>> void msdRadixSortStable(List<T> list) {
        msdRadixSortStableBy(list, Function.identity());
    }

    static <T, V extends BitComparable<V>> void msdRadixSortStableBy(T[] array, Function<T, V> compareBy, int start, int end, int position, T[] equal, T[] notEqual) {
        var contentLength = end - start;
        if (contentLength <= 1) {
            return;
        }
        var comparisonHolder = array[start];
        var comparisonElement = compareBy.apply(comparisonHolder);
        if (comparisonElement.getBitCount() <= position) {
            //No more bits to sort for.
            return;
        }

        //Sort the contents into two parts: elements that are equal to the first one (at the current bit position) and
        // elements that are not.
        var equalsIndex = 0;
        var notEqualsIndex = 0;

        for (int i = start + 1; i < end; i++) {
            var currentHolder = array[i];
            var current = compareBy.apply(currentHolder);
            if (comparisonElement.areBitsEqual(current, position)) {
                equal[equalsIndex++] = currentHolder;
            } else {
                notEqual[notEqualsIndex++] = currentHolder;
            }
        }

        int splitPosition;
        //If all elements are equals to the first one in the current index, they are already sorted.
        if (notEqualsIndex != 0) {
            //Determine if the first element corresponds to the 0 or 1 bit.
            var result = comparisonElement.compareBits(compareBy.apply(notEqual[0]), position);
            assert result != 0;
            if (result < 0) {
                //first element corresponds to the 0-bit.
                System.arraycopy(equal, 0, array, start + 1, equalsIndex);
                System.arraycopy(notEqual, 0, array, start + 1 + equalsIndex, notEqualsIndex);
                splitPosition = start + 1 + equalsIndex;
            } else {
                //First element corresponds to the 1-bit.
                array[start + notEqualsIndex] = array[start];
                System.arraycopy(notEqual, 0, array, start, notEqualsIndex);
                System.arraycopy(equal, 0, array, start + notEqualsIndex + 1, equalsIndex);
                splitPosition = start + notEqualsIndex;
            }
        } else {
            splitPosition = end;
        }
        msdRadixSortStableBy(array, compareBy, start, splitPosition, position + 1, equal, notEqual);
        msdRadixSortStableBy(array, compareBy, splitPosition, end, position + 1, equal, notEqual);
    }

    static <T, V extends BitComparable<V>> void msdRadixSortInplaceBy(List<T> list, Function<T, V> compareBy) {
        var array = list.toArray();
        msdRadixSortInplaceBy((T[])array, compareBy, 0, array.length, 0);
        list.clear();
        list.addAll((List<T>) Arrays.asList(array));
    }

    static <T extends BitComparable<T>> void msdRadixSortInplace(List<T> list) {
        msdRadixSortInplaceBy(list, Function.identity());
    }

    static <T, V extends BitComparable<V>> void msdRadixSortInplaceBy(T[] array, Function<T, V> compareBy, int start, int end, int position) {
        var contentLength = end - start;
        if (contentLength <= 1) {
            return;
        }
        var comparisonHolder = array[start];
        var comparisonElement = compareBy.apply(comparisonHolder);
        if (comparisonElement.getBitCount() <= position) {
            //No more bits to sort for.
            return;
        }

        var endIndex = end;
        var beginIndex = start + 1;
        while (beginIndex < endIndex) {
            var currentHolder = array[beginIndex];
            var current = compareBy.apply(currentHolder);
            if (comparisonElement.areBitsEqual(current, position)) {
                beginIndex++;
            } else {
                endIndex--;
                array[beginIndex] = array[endIndex];
                array[endIndex] = currentHolder;
            }
        }
        var split = beginIndex;
        var last = compareBy.apply(array[end - 1]);
        if (comparisonElement.compareBits(last, position) > 0) {
            split = end - beginIndex + start;
            var toSwap = Math.min(end - endIndex, beginIndex - start);
            for (int i = 0; i < toSwap; i++) {
                var tmp = array[start + i];
                array[start + i] = array[end - i - 1];
                array[end - i - 1] = tmp;
            }
        }
        msdRadixSortInplaceBy(array, compareBy, start, split, position + 1);
        msdRadixSortInplaceBy(array, compareBy, split, end, position + 1);
    }

}
