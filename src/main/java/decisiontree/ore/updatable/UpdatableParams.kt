package decisiontree.ore.updatable

import crypto.ore.UpdatableCiphertext
import crypto.ore.UpdatableSecretKey
import decisiontree.math.Group
import decisiontree.ore.OreParams
import decisiontree.ore.prf.KeyHomomorphicPrf
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.SecureRandom
import kotlin.jvm.Throws

data class UpdatableParams<D>(val prf: KeyHomomorphicPrf<D>, val messageBitLength: Int) :
    OreParams<UpdatableCiphertext<D>> {
    internal val prfInputLengthInBytes: Int
        get() = inputBytes + 1
    internal val inputBytes: Int
        get() = (messageBitLength + 7) / 8
    internal val group: Group<D>
        get() = prf.group

    override fun generateSecretKey(random: SecureRandom): UpdatableSecretKey<D> {
        return UpdatableSecretKey(this, prf.generateKey(random))
    }


    @Throws(IOException::class)
    fun writeCiphertext(outs: ObjectOutputStream, ct: UpdatableCiphertext<D>) {
        require(ct.buffer.size == messageBitLength)
        ct.buffer.forEach {
            group.writeElement(outs, it)
        }
    }
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readElement(ins: ObjectInputStream): UpdatableCiphertext<D> {
        val list = ArrayList<D>(messageBitLength)
        for (i in 0 until messageBitLength) {
            list += group.readElement(ins)
        }
        return UpdatableCiphertext(list, group)
    }

}