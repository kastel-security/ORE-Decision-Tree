package decisiontree.math

import com.squareup.jnagmp.Gmp
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

typealias ZpElement = BigInteger

/**
 * This implements the square residue subgroup of the multiplicative group Zp, where p is safe prime.
 * All operations perform sqrt(x^2) first, where sqrt returns the square root element in the group.
 * This way, all invalid elements are first mapped onto valid elements.
 */
data class ZSafePrime(private val p: BigInteger): Group<ZpElement>, Serializable {
    @Transient
    private val pMinusOneHalf: BigInteger = (p - BigInteger.ONE) / BigInteger.TWO
    @Transient
    private val inverseTwo: BigInteger = Gmp.modInverse(BigInteger.TWO, pMinusOneHalf)
    override val groupOrder: BigInteger
        get() = pMinusOneHalf

    init {
        require(p.isProbablePrime(100))
        require(pMinusOneHalf.isProbablePrime(100))
        // p needs to be safe prime.
    }

    companion object {
        @JvmField
        val p2048 = BigInteger("18597436170813239740761094701404437740269282886857767482354226589803515563256958652651161788508275472215387234728508487131077027218345319508140580855871091460726080055253882156190483042546163299359796085642959976207769679890525502324709118394969845675832151534156979433225637335519247102957868538104462272399461609240139060911339357536296800958244698952041716369663004275493540219341398422035545588477718415452826158800773345837937510112514937965279755056131265513991889382045065955140509713472327122320644260927236016659526703043872856926268568052738398751134409224527392228257239475973781682671844860414226372855007")
//        val p2048 = getSafePrime(2048)
        val p2048Group = ZSafePrime(p2048)

        fun getSafePrime(nBits: Int, rand: Random = Random()): BigInteger {
            while (true) {
                val prime = BigInteger.probablePrime(nBits - 1, rand)
                val maybeSafePrime = prime * BigInteger.TWO + BigInteger.ONE
                if (maybeSafePrime.isProbablePrime(100)) {
                    return maybeSafePrime
                }
            }
        }
    }

    override fun sampleElement(rand: SecureRandom): ZpElement {
        val ele = sampleBigInt(rand, p) { true }
        return (ele.multiply(ele)) % p
    }

    override fun readElement(ins: ObjectInputStream): ZpElement {
        val ret = ins.readObject() as ZpElement
        require(ret >= BigInteger.ZERO)
        require(ret < p)
        return ret
    }

    override fun writeElement(outs: ObjectOutputStream, element: ZpElement) {
        return outs.writeObject(element)
    }

    override fun ZpElement.times(scalar: BigInteger): ZpElement {
        val doubleElement = this.multiply(this)
        //Since we ignore sidechannels, we can use modPowInsecure without any security implications.
        return Gmp.modPowInsecure(doubleElement, scalar.multiply(inverseTwo) % pMinusOneHalf, p)
    }

    override fun ZpElement.plus(other: ZpElement): ZpElement {
        return (this.multiply(other)) % p
    }
}