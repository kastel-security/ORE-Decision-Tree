package decisiontree.main

import crypto.ore.UpdatableCiphertext
import crypto.ore.UpdatableSecretKey
import decisiontree.math.Ed25519Group
import decisiontree.ore.OreParams
import decisiontree.ore.clww.ClwwParams
import decisiontree.ore.prf.NPRPrf
import decisiontree.ore.prf.Prf
import decisiontree.ore.prf.SeededRandom
import decisiontree.ore.updatable.UpdatableParams
import decisiontree.util.BitComparable
import decisiontree.util.benchmark
import decisiontree.util.timed
import java.security.SecureRandom

fun main() {
    val rand = SeededRandom(Prf.AES_128, ByteArray(32))
    val clwwParams = ClwwParams(Prf.HMAC_SHA256, 32)
    val updatable = UpdatableParams(NPRPrf(NPRPrf.SHA256, Ed25519Group, Prf.HMAC_SHA256), 32)

    oreBenchmark("CLWW", clwwParams, rand, 1_000, 10)
    oreBenchmark("Updatable", updatable, rand, 1_000, 10)
}

fun <T : BitComparable<T>> oreBenchmark(scheme: String, params: OreParams<T>, rand: SecureRandom, n: Int, time: Int)
        = timed("Benchmark of $scheme scheme"){
    val key = params.generateSecretKey(rand)
    val values = IntArray(n) {
        rand.nextInt()
    }
    val cts = ArrayList<T>(n)
    timed("Setting up ciphertext list") {
        for (i in 0 until n) {
            cts += key.encrypt(values[i].toLong() and 0xFFFFFFFF)
        }
    }
    benchmark("Encryptions with scheme $scheme", time) {
        val i = it.toInt()
        key.encrypt(values[i % n].toLong() and 0xFFFFFFFF)
    }
    if (params is UpdatableParams<*>) {
        val key = key as UpdatableSecretKey<Any>
        val cts = cts as List<UpdatableCiphertext<Any>>
        benchmark("Updates with scheme $scheme", time) {
            key.update(cts[(it % n).toInt()])
        }
        benchmark("Inverse update with $scheme", time) {
            key.updateRev(cts[(it % n).toInt()])
        }
    }

    benchmark("Comparisons with scheme $scheme", time) {
        val i = it.toInt()
        cts[i % n].compareTo(cts[(i + 1) % n])
    }

    benchmark("Sorting $n values with Collections.sort and scheme $scheme", time) {
        val copiedList = ArrayList(cts)
        copiedList.sort()
    }
    benchmark("Sorting $n values with MSD-Radixsort (stable) and scheme $scheme", time) {
        val copiedList = ArrayList(cts)
        BitComparable.msdRadixSortStable(copiedList)
    }
    benchmark("Sorting $n values with MSD-Radixsort (inplace) and scheme $scheme", time) {
        val copiedList = ArrayList(cts)
        BitComparable.msdRadixSortInplace(copiedList)
    }
}
