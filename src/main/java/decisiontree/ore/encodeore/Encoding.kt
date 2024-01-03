package decisiontree.ore.encodeore

interface Encoding {
    fun encode(msg: Long): Long {
        val (exponent, value) = encodeExponentValue(msg)
        val shiftedExponent = exponent shl valueLength
        val maskedValue = value and (1L shl valueLength) - 1
        return shiftedExponent.toLong() or maskedValue
    }

    fun encodeExponentValue(msg: Long): Pair<Int, Long>
    fun decode(range: Int, value: Long): Long
    fun decode(encoded: Long): Long {
        val valuePart = encoded and (1L shl valueLength) - 1
        val rangePart = encoded shr valueLength and (1L shl rangeLength) - 1
        return decode(rangePart.toInt(), valuePart)
    }

    val rangeLength: Int
    val valueLength: Int
    val totalLength: Int
        get() = rangeLength + valueLength
}