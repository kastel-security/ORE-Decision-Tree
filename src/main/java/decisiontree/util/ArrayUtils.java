package decisiontree.util;

import java.util.Random;
import java.util.stream.IntStream;

public class ArrayUtils {

    public static int indexOf(int[] arr, int val) {
        for (int i = 0; i < arr.length; i++) {
            if (val == arr[i]) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    public static int[] permutation(int n, Random rng) {
        var array = IntStream.range(0, n).toArray();
        for (int i = 0; i < array.length - 1; i++) {
            var current = array[i];
            var swapIdx = rng.nextInt(array.length - i);
            array[i] = array[swapIdx];
            array[swapIdx] = current;
        }
        return array;
    }
}