package decisiontree.math

import com.squareup.jnagmp.Gmp
import org.bouncycastle.math.ec.ECConstants.EIGHT
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.util.BigIntegers
import java.io.ObjectInputStream
import java.math.BigInteger

object Curve25519Group: ECGroup("curve25519") {
    @Transient
    private val inverse = Gmp.modInverse(EIGHT, groupOrder)

    override fun verifyOrder(point: ECPoint): ECPoint? {
        //This makes sure we are in the correct subgroup
        return point.multiply(EIGHT)
    }

    override fun ECPoint.times(scalar: BigInteger): ECPoint {
        val eightPoint = this.timesPow2(3)
        return eightPoint.multiply((scalar * inverse) % groupOrder)
    }

    override fun readElement(ins: ObjectInputStream): ECPoint {
        val encoded = ins.readObject() as ByteArray
    //        point = curve25519.curve.decodePoint(encoded)

        // This is a copy of decodePoint, with the small adjustment, that we do not check if the point has the correct
        // order. This is not necessary, because we adjust subsequent computations to first put the point into the
        // correct subgroup. See #times.
        val expectedLength = (curve.fieldSize + 7) / 8
        val type = encoded[0].toInt()
        require(encoded.size == expectedLength + 1) { "Incorrect length for compressed encoding" }
        require(type == 0x02 || type == 0x03) { "Invalid point encoding 0x" + Integer.toString(type, 16) }

        val yTilde: Int = type and 1
        val X = BigIntegers.fromUnsignedByteArray(encoded, 1, expectedLength)
        val decompressPoint = ECCurve::class.java.getDeclaredMethod("decompressPoint",
            Int::class.javaPrimitiveType, BigInteger::class.java)
        decompressPoint.isAccessible = true
        val p = decompressPoint(curve, yTilde, X) as ECPoint
        val satisfiesCurveEquation = ECPoint::class.java.getDeclaredMethod("satisfiesCurveEquation")
        satisfiesCurveEquation.isAccessible = true
        require(satisfiesCurveEquation(p) as Boolean) { "Point does not satisfy curve equation." }
        return p
    }

    private fun readResolve(): Any {
        return Curve25519Group
    }
}