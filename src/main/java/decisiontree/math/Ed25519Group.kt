package decisiontree.math

import com.squareup.jnagmp.Gmp
import decisiontree.math.Ed25519.*
import decisiontree.util.WrappedByteArray
import org.bouncycastle.math.ec.ECConstants.EIGHT
import org.bouncycastle.util.encoders.Hex
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.math.BigInteger
import java.security.SecureRandom
import kotlin.experimental.and

object Ed25519Group: Group<WrappedByteArray>, Serializable {
    @Transient
    override val groupOrder: BigInteger = BigInteger.ONE.shiftLeft(252) + BigInteger("27742317777372353535851937790883648493")
    @Transient
    val basePoint = WrappedByteArray(Hex.decode("4cb5abf6ad79fbf5abbccafcc269d85cd2651ed4b885b5869f241aedf0a5ba29"))
    @Transient
    val infinity = WrappedByteArray(ByteArray(32) { if (it == 0) 1 else 0})
    @Transient
    private val inverse = Gmp.modInverse(EIGHT, groupOrder)
    override fun sampleScalar(rand: SecureRandom): BigInteger {
        val bytes = ByteArray(SECRET_KEY_SIZE)
        generatePrivateKey(rand, bytes)
        return bytesToBigInt(bytes)
    }

    override fun sampleElement(rand: SecureRandom): WrappedByteArray {
        val array = ByteArray(PUBLIC_KEY_SIZE)
        do {
            rand.nextBytes(array)
        } while (!validatePublicKeyPartial(array, 0))
        return timesTwoPow(WrappedByteArray(array), 3)
    }

    override fun readElement(ins: ObjectInputStream): WrappedByteArray {
        val array = ins.readNBytes(PUBLIC_KEY_SIZE)
        require(validatePublicKeyPartial(array, 0))
        return WrappedByteArray(array)
    }

    override fun writeElement(outs: ObjectOutputStream, element: WrappedByteArray) {
        require(element.array.size == PUBLIC_KEY_SIZE)
        outs.write(element.array)
    }

    override fun WrappedByteArray.times(scalar: BigInteger): WrappedByteArray {
        val currentPoint = decodePoint(timesTwoPow(this, 3))
        val result = scalarMult(bigIntToBytes((scalar * inverse) % groupOrder), currentPoint)
        return encodePoint(result)
    }

    override fun WrappedByteArray.plus(other: WrappedByteArray): WrappedByteArray {
        if (other === this) {
            return timesTwoPow(this, 1)
        }
        val first = decodePoint(this).toExtendedPoint()
        val second = decodePoint(other).toExtendedPoint()
        pointAdd(first, second, first, PointTemp())
        return encodePoint(first.toAccum())
    }

    fun timesTwoPow(arg: WrappedByteArray, n: Int): WrappedByteArray {
        var current = decodePoint(arg).toExtendedPoint().toAccum()
        for (i in 0 until n) {
            pointDouble(current)
        }
        return encodePoint(current)
    }

    private inline fun decodePoint(bytes: WrappedByteArray): PointAffine {
        val ret = PointAffine()
        decodePointVar(bytes.array, 0, false, ret)
        return ret
    }

    private inline fun scalarMult(scalar: ByteArray, point: PointAffine): PointAccum {
        val ret = PointAccum()
        scalarMult(scalar, point, ret)
        return ret
    }

    private inline fun encodePoint(point: PointAccum): WrappedByteArray {
        val ret = ByteArray(PUBLIC_KEY_SIZE)
        encodePoint(point, ret, 0)
        return WrappedByteArray(ret)
    }

    private inline fun PointAffine.toExtendedPoint(): PointExtended {
        val ret = PointExtended()
        pointCopy(this, ret)
        return ret
    }

    private inline fun PointExtended.toAccum(): PointAccum {
        val ret = PointAccum()
        F.copy(x, 0, ret.x, 0)
        F.copy(y, 0, ret.y, 0)
        F.copy(z, 0, ret.z, 0)
        return ret
    }

    private fun bytesToBigInt(bytes: ByteArray): BigInteger {
        require(bytes.size == SECRET_KEY_SIZE)
        bytes.reverse()
        bytes[0] = bytes[0] and 0x7F
        return BigInteger(bytes)
    }

    private fun bigIntToBytes(bigInt: BigInteger): ByteArray {
        val ret = ByteArray(SECRET_KEY_SIZE)
        val bytes = bigInt.toByteArray()
        require(bytes.size <= 32)
        System.arraycopy(bytes, 0, ret, SECRET_KEY_SIZE - bytes.size, bytes.size)
        ret.reverse()
        return ret
    }

    private fun readResolve(): Any {
        return Ed25519Group
    }
}