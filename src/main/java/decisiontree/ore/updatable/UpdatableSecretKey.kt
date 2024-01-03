package crypto.ore

import com.squareup.jnagmp.Gmp
import decisiontree.ore.OreSecretKey
import decisiontree.ore.updatable.UpdatableParams
import java.math.BigInteger
import java.nio.ByteBuffer

class UpdatableSecretKey<D> internal constructor(private val params: UpdatableParams<D>, private val prfKey: BigInteger): OreSecretKey<UpdatableCiphertext<D>> {

    private val invertedKey: BigInteger by lazy {
        Gmp.modInverse(prfKey, params.group.groupOrder)
    }

    private fun getBitAndPrfInput(target: ByteArray, msg: Long, i: Byte): Byte {
        assert(target.size == params.prfInputLengthInBytes)
        val buffer = ByteBuffer.wrap(target)
        val ret = (msg shr params.messageBitLength - i - 1 and 1L).toByte()
        var mask = 0L
        if (i > 0) {
            mask = Long.MIN_VALUE ushr i - 1
        }
        val targetMsg = mask and msg
        buffer.put(i)
        for (j in 0 until params.prfInputLengthInBytes - 1) {
            buffer.put((targetMsg shr j).toByte())
        }
        return ret
    }

    override fun encrypt(msg: Long): UpdatableCiphertext<D> {
        val LENGTH = params.messageBitLength
        require(!(msg < 0 || msg > (1L shl LENGTH) - 1)) { "Message $msg is outside of the allwed range for this encryption parameter set (min 0, max ${(1L shl LENGTH) - 1}.)" }
        val res = ArrayList<D>()
        val prfInput = ByteArray(params.prfInputLengthInBytes)
        for (i in 0 until LENGTH) {
            val bit = getBitAndPrfInput(prfInput, msg, i.toByte())
            val prfOutput = params.prf.evaluate(prfKey, prfInput)
            val currentResult = if (bit == 0.toByte()) prfOutput else params.group.run {
                prfOutput + prfOutput
            }
            res.add(currentResult)
        }
        return UpdatableCiphertext(res, params.group)
    }

    override fun decrypt(ct: UpdatableCiphertext<D>): Long {
        var result = 0L
        val prfInput = ByteArray(params.prfInputLengthInBytes)
        for (i in 0 until params.messageBitLength) {
            getBitAndPrfInput(prfInput, result, i.toByte())
            val prfOutput = params.prf.evaluate(prfKey, prfInput)
            if (ct.buffer[i] == params.group.run { prfOutput + prfOutput }) {
                result += 1L shl (params.messageBitLength - 1 - i)
            } else {
                require(ct.buffer[i] == prfOutput) { "Invalid Ciphertext." }
            }
        }
        return result
    }

    private fun updateWithKey(ct: UpdatableCiphertext<D>, key: BigInteger): UpdatableCiphertext<D> {
        require(ct.group == params.group)
        val res = ArrayList<D>(ct.buffer.size)
        for (i in 0 until ct.buffer.size) {
            val current = ct.buffer[i]
            params.group.apply {
                res += current.times(key)
            }
        }
        return UpdatableCiphertext(res, params.group)
    }

    fun update(ct: UpdatableCiphertext<D>): UpdatableCiphertext<D> {
        return updateWithKey(ct, prfKey)
    }

    fun updateRev(ct: UpdatableCiphertext<D>): UpdatableCiphertext<D> {
        return updateWithKey(ct, invertedKey)
    }
}