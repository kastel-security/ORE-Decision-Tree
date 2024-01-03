package decisiontree.ore.encodeore

class BinaryEncoding(bitLen: Int) : Encoding {
    private val maxValue: Long
    override val valueLength: Int
    private val maxExponent: Int
    override val rangeLength: Int

    init {
        maxValue = (1L shl bitLen) - 1
        valueLength = Math.max(Math.ceil(log2((maxValue + 1).toDouble())).toInt() - 1, 1)
        maxExponent = Math.floor(log2(maxValue.toDouble())).toInt()
        rangeLength = Math.ceil(log2((maxExponent + 1).toDouble())).toInt()
    }

    override fun encodeExponentValue(msg: Long): Pair<Int, Long> {
        val exponent: Int
        val adjustedMessage: Long
        //We need to treat 0 differently, because 0 cannot be represented as 1.xxxxxxx * d ^ yyyyyyy
        //Therefore we de-normalize 0. For the comparison to still work, we also de-normalize 1.
        if (msg > maxValue) {
            throw IllegalArgumentException("Message $msg too large. Max allowed is $maxValue.")
        } else //De-normalized
            require(msg >= 0) { "Negative number: $msg" }
        if (msg <= 1) {
            //De-normalized
            exponent = 0
            adjustedMessage = msg shl valueLength - 1
        } else {
            exponent = (Math.log(msg.toDouble()) / log2 + 1e-15).toInt()
            val shAmt = maxExponent - exponent
            adjustedMessage = msg shl shAmt and (1L shl valueLength) - 1
        }
        assert(exponent <= maxExponent)
        return Pair(exponent, adjustedMessage)
    }

    override fun decode(range: Int, value: Long): Long {
        var value = value
        if (range == 0) {
            return if (value != 0L) 1 else 0
        }
        value = value or (1L shl valueLength) // normalizer-bit
        return value shr maxExponent - range
    }

    companion object {
        private val log2 = Math.log(2.0)
        fun log2(value: Double): Double {
            return Math.log(value) / log2
        }
    }
}