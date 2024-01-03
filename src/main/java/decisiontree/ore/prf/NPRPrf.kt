package decisiontree.ore.prf

import decisiontree.math.Group
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * PRF from Naor, Pinkas, Reingold 99
 */
class NPRPrf<T>(mdName: String, private val group: Group<T>, private val keyGenPrf: Prf<out Any, ByteArray>): KeyHomomorphicPrf<T> {
    companion object {
        const val SHA256 = "SHA-256"
        const val SHA512 = "SHA-512"
        const val SHA3_256 = "SHA3-256"
        const val SHA3_512 = "SHA3-512"
    }
    private val md = ThreadLocal.withInitial {
        MessageDigest.getInstance(mdName)
    }

    override fun evaluate(prfKey: BigInteger, input: ByteArray): T {
        val digest = md.get().digest(input)
        val rand = SeededRandom(keyGenPrf, digest)
        val sampledElement = group.sampleElement(rand)
        group.apply {
            return sampledElement * prfKey
        }
    }

    override fun generateKey(rand: SecureRandom): BigInteger {
        return group.sampleScalar(rand)
    }

    override fun getKey(keyBytes: ByteArray): BigInteger {
        return BigInteger(keyBytes)
    }

    override fun getSecPar(): Int {
        return group.groupOrder.bitLength()
    }

    override fun getGroup(): Group<T> {
        return group
    }
}