package decisiontree.ore.clww

import decisiontree.ore.OreParams
import decisiontree.ore.OreSecretKey
import decisiontree.ore.prf.Prf
import java.security.SecureRandom

class ClwwParams<K>(
    val prf: Prf<K, ByteArray>, val messageBitLength: Int
) : OreParams<ClwwCiphertext> {

    override fun generateSecretKey(random: SecureRandom): OreSecretKey<ClwwCiphertext> {
        return ClwwSecretKey(this, prf.generateKey(random))
    }

    companion object {
        const val MODULUS = 4
    }
}