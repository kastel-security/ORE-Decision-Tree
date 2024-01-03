package crypto.ore

import decisiontree.math.Group
import decisiontree.util.BitComparable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

data class UpdatableCiphertext<D>(internal var buffer: List<D>, internal var group: Group<D>) : BitComparable<UpdatableCiphertext<D>>, Serializable {

    override fun compareTo(other: UpdatableCiphertext<D>): Int {
        require(buffer.size == other.buffer.size)
        for (i in buffer.indices) {
            val comparison = compareBits(other, i)
            if (comparison != 0) {
                return comparison
            }
        }
        return 0
    }

    private fun readObject(ins: ObjectInputStream) {
        group = ins.readObject() as Group<D>
        val n = ins.readInt()
        val list = ArrayList<D>(n)
        for (i in 0 until n) {
            list += group.readElement(ins)
        }
        buffer = list
    }

    private fun writeObject(outs: ObjectOutputStream) {
        outs.writeObject(group)
        outs.writeInt(buffer.size)
        for (element in buffer) {
            group.writeElement(outs, element)
        }
    }

    override fun getBitCount(): Int {
        return buffer.size
    }

    override fun compareBits(other: UpdatableCiphertext<D>, position: Int): Int {
        val ours = buffer[position]
        val otherElement = other.buffer[position]
        if (ours == otherElement) {
            return 0
        } else if (ours == group.run { otherElement + otherElement }) {
            return 1
        } else {
//                require (group.run { ours + ours } == otherElement)
            return -1
        }
    }

    override fun areBitsEqual(other: UpdatableCiphertext<D>, position: Int): Boolean {
        return buffer[position] == other.buffer[position]
    }
}