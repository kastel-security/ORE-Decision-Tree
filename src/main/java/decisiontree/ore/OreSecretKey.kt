package decisiontree.ore

import java.util.function.LongFunction
import java.util.function.ToIntBiFunction

interface OreSecretKey<T : Comparable<T>?> {
    fun encrypt(msg: Long): T
    fun decrypt(ct: T): Long
    fun decrypt(ct: T, min: Long, max: Long): Long {
        return decrypt(ct, min, max, { msg: Long -> encrypt(msg) }) { obj: T, t: T -> obj!!.compareTo(t) }
    }

    companion object {
        fun <L, R> decrypt(ct: L, min: Long, max: Long, enc: (Long) -> R, compare: (L, R) -> Int): Long {
            var min = min
            var max = max
            assert(compare(ct, enc(min)) >= 0)
            assert(compare(ct, enc(max)) <= 0)
            while (min <= max) {
                val mid = (max - min) / 2 + min
                val midCt = enc(mid)
                val cmp = compare(ct, midCt)
                if (cmp == 0) {
                    return mid
                } else if (cmp < 0) {
                    max = mid - 1
                } else {
                    min = mid + 1
                }
            }
            throw IllegalStateException()
        }
    }
}