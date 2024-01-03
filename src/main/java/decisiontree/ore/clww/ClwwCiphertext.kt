package decisiontree.ore.clww

import decisiontree.util.BitComparable
import java.util.*

data class ClwwCiphertext(internal val buffer: ByteArray) : BitComparable<ClwwCiphertext> {
    override fun compareTo(other: ClwwCiphertext): Int {
        require(buffer.size == other.buffer.size)
        for (i in buffer.indices) {
        }
        return 0
    }

    override fun getBitCount(): Int = buffer.size

    override fun compareBits(other: ClwwCiphertext, position: Int): Int {
        val ours = buffer[position].toInt() and 0xFF
        val otherVal = other.buffer[position].toInt() and 0xFF
        if (ours == (otherVal + 1) % ClwwParams.MODULUS) {
            return 1
        } else if ((ours + 1) % ClwwParams.MODULUS == otherVal) {
            return -1
        } else{
            require(ours == otherVal) { "Malformed ciphertext." }
            return 0
        }
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(buffer)
    }

    override fun equals(other: Any?): Boolean {
        return other is ClwwCiphertext && Arrays.equals(buffer, other.buffer)
    }
}