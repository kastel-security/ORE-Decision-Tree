package decisiontree.ore.clww

import decisiontree.ore.OreSecretKey

class ClwwSecretKey<K> internal constructor(private val params: ClwwParams<K>, private val prfKey: K) :
    OreSecretKey<ClwwCiphertext> {
    private val messageLengthInBits: Int
        get() = params.messageBitLength
    private val prfInputLengthInBytes: Int
        get() = inputBytes + 1
    private val inputBytes: Int
        get() = (messageLengthInBits + 7) / 8

    private fun writeToByteArray(target: ByteArray, offset: Int, msg: Long) {
        for (i in 0 until inputBytes) {
            target[offset + i] = (msg shr 56 - 8 * i and 0xFFL).toByte()
        }
    }

    private fun getBitAndPrfInput(target: ByteArray, msg: Long, i: Byte): Byte {
        assert(target.size == prfInputLengthInBytes)
        val ret = (msg shr messageLengthInBits - i - 1 and 1L).toByte()
        var mask = 0L
        if (i > 0) {
            mask = Long.MIN_VALUE ushr i - 1
        }
        val targetMsg = mask and msg
        target[0] = i
        writeToByteArray(target, 1, targetMsg)
        return ret
    }

    private fun getPrfOutput(input: ByteArray): Byte {
        assert(ClwwParams.MODULUS == 4)
        val out = params.prf.evaluate(prfKey, input)
        return (out[0].toInt() and 3).toByte()
    }

    override fun encrypt(msg: Long): ClwwCiphertext {
        val LENGTH = messageLengthInBits
        require(!(msg < 0 || msg > (1L shl LENGTH) - 1)) { "Invalid message to encrypt: $msg" }
        val res = ByteArray(LENGTH)
        val prfInput = ByteArray(prfInputLengthInBytes)
        for (i in 0 until LENGTH) {
            val bit = getBitAndPrfInput(prfInput, msg, i.toByte())
            res[i] = ((getPrfOutput(prfInput) + bit) % ClwwParams.MODULUS).toByte()
        }
        return ClwwCiphertext(res)
    }

    override fun decrypt(ct: ClwwCiphertext): Long {
        return decrypt(ct, 0, (1L shl messageLengthInBits) - 1)
    }
}