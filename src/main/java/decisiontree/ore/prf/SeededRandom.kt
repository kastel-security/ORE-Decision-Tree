package decisiontree.ore.prf

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.SecureRandomParameters

class SeededRandom<K>(private val prf: Prf<K, ByteArray>, private val seed: K): SecureRandom() {
    private val currentIndex = ByteBuffer.allocate(4)
    private var buffer = byteArrayOf()
    private var bufferPos = 0
    private val initialized = true

    constructor(prf: Prf<K, ByteArray>, seed: ByteArray): this(prf, prf.getKey(seed))

    private fun getNextByte(): Byte {
        if (bufferPos < buffer.size) {
            return buffer[bufferPos++]
        }
        currentIndex.clear()
        buffer = prf.evaluate(seed, currentIndex.array())
        bufferPos = 0

        val newIndex = currentIndex.getInt() + 1
        if (newIndex == 0) {
            throw ArrayIndexOutOfBoundsException("PRG exhausted.")
        }
        currentIndex.clear()
        currentIndex.putInt(newIndex)
        return getNextByte()
    }

    override fun getAlgorithm(): String {
        return "$prf"
    }

    override fun toString(): String {
        return "PRF-RNG: $prf"
    }

    override fun getParameters(): SecureRandomParameters {
        throw UnsupportedOperationException()
    }

    override fun setSeed(seed: ByteArray) {
        throw UnsupportedOperationException()
    }

    override fun setSeed(seed: Long) {
        //This is necessary, because this method is called in the superconstructor
        if (initialized) throw UnsupportedOperationException()
    }

    override fun nextBytes(bytes: ByteArray) {
        for (i in 0 until bytes.size) {
            bytes[i] = getNextByte()
        }
    }

    override fun nextBytes(bytes: ByteArray, params: SecureRandomParameters?) {
        throw UnsupportedOperationException()
    }

    override fun generateSeed(numBytes: Int): ByteArray {
        val ret = ByteArray(numBytes)
        nextBytes(ret)
        return ret
    }

    override fun reseed() {
        throw UnsupportedOperationException()
    }

    override fun reseed(params: SecureRandomParameters?) {
        throw UnsupportedOperationException()
    }
}