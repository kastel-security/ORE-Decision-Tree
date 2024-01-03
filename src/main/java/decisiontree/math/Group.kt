package decisiontree.math

import com.squareup.jnagmp.Gmp
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigInteger
import java.math.BigInteger.*
import java.security.SecureRandom
import java.util.*
import kotlin.jvm.Throws


inline fun sampleBigInt(rand: Random, max: BigInteger, predicate: (BigInteger) -> Boolean): BigInteger {
    while (true) {
        val candidate = BigInteger(max.bitLength(), rand)
        if (candidate < max && predicate(candidate)) {
            return candidate
        }
    }
}
private fun numbers(start: BigInteger): Sequence<BigInteger> = sequence {
    yield(start)
    yieldAll(numbers(start + ONE))
}

fun sqrt(element: BigInteger, p: BigInteger): Set<BigInteger> {
    require(p.isProbablePrime(100)) { "$p is not prime." }
    val pMinusOne = p - ONE
    val pMinusOneHalf = pMinusOne / TWO
    val oddDivisor = pMinusOne / TWO.pow(pMinusOne.lowestSetBit)
    if (Gmp.modPowInsecure(element, pMinusOneHalf, p) != ONE) {
        return setOf() // not a square.
    }
    val inverseTwo = (oddDivisor + ONE) / TWO // Gmp.modInverse(TWO, oddDivisor)
    val root =
        if (Gmp.modPowInsecure(element, oddDivisor, p) == ONE) {
            Gmp.modPowInsecure(element, inverseTwo, p)
        } else {
            val nonSquare = sampleBigInt(Random(), p) { Gmp.modPowInsecure(it, pMinusOneHalf, p) == pMinusOne }
            val toCompute = element * nonSquare * nonSquare % p
            Gmp.modPowInsecure(toCompute, inverseTwo, p) * Gmp.modInverse(nonSquare, p) % p
        }
    return setOf(root, p - root)
}

interface Group<T> {

    val groupOrder: BigInteger

    operator fun T.plus(other: T): T
    operator fun T.times(scalar: BigInteger): T
//    operator fun T.unaryMinus(): T
    fun sampleElement(rand: SecureRandom): T
    fun sampleScalar(rand: SecureRandom): BigInteger {
        return sampleBigInt(rand, groupOrder) {
            Gmp.gcd(it, groupOrder) == BigInteger.ONE
        }
    }

    @Throws(IOException::class)
    fun writeElement(outs: ObjectOutputStream, element: T)
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readElement(ins: ObjectInputStream): T
}