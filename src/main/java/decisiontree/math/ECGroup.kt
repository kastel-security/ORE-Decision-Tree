package decisiontree.math

import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECFieldElement
import org.bouncycastle.math.ec.ECPoint
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.math.BigInteger
import java.security.SecureRandom

open class ECGroup(private var curveName: String): Group<ECPoint>, Serializable {

    @Transient
    private var lazyCurve: ECCurve? = null
    val curve: ECCurve
        get() {
            var curve = lazyCurve
            if (lazyCurve == null) {
                curve = CustomNamedCurves.getByName(curveName).curve
                lazyCurve = curve
            }
            return curve!!
        }
    override val groupOrder: BigInteger
        get() = curve.order

    override fun sampleElement(rand: SecureRandom): ECPoint {
        while (true) {
            val ySign = rand.nextBoolean()
            val X = curve.randomFieldElementMult(rand)
            val point = getPointFromXValue(X, ySign)
            if (point == null) {
                //This happens if the curve equation cannot be satisfied for this X
                continue
            }
            //Now point is on the curve.

            val returnPoint = verifyOrder(point)
            if (returnPoint != null) {
//                assert(returnPoint.isValid)
                return returnPoint
            }
        }
    }

    override fun readElement(ins: ObjectInputStream): ECPoint {
        return curve.decodePoint(ins.readObject() as ByteArray)
    }

    override fun writeElement(outs: ObjectOutputStream, element: ECPoint) {
        outs.writeObject(element.getEncoded(true))
    }

    open fun verifyOrder(point: ECPoint): ECPoint? {
        if (point.multiply(groupOrder).isInfinity) {
            return point
        } else {
            return null
        }
    }

    private fun getPointFromXValue(X: ECFieldElement, yPositive: Boolean): ECPoint? {
        // Y^2 = X^3 + A*X + B
        val A = curve.a
        val B = curve.b

        val Ysq = X.square().add(A).multiply(X).add(B) //Curve equation
        val plusY = Ysq.sqrt()
        if (plusY == null) {
            return null
        } else {
            val Y =  if (yPositive) plusY else plusY.negate()
            return curve.createPoint(X.toBigInteger(), Y.toBigInteger())
        }
    }

    override fun ECPoint.times(scalar: BigInteger): ECPoint {
        return this.multiply(scalar)
    }

    override fun ECPoint.plus(other: ECPoint): ECPoint {
        return this.add(other)
    }

    override fun equals(other: Any?): Boolean {
        return other is ECGroup && other.curveName == curveName
    }

    override fun hashCode(): Int {
        return curveName.hashCode()
    }
}