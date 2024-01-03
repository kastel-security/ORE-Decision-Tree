package decisiontree.ore.encodeore

import decisiontree.ore.OreParams
import decisiontree.ore.OreSecretKey
import decisiontree.ore.clww.ClwwCiphertext
import decisiontree.ore.clww.ClwwParams
import decisiontree.ore.prf.Prf
import java.security.SecureRandom

class EncodeOreParams<K>(prf: Prf<K, ByteArray>, val encoding: Encoding) : OreParams<ClwwCiphertext> {
    private val baseParams: ClwwParams<K>

    init {
        baseParams = ClwwParams(prf, encoding.totalLength)
    }

    override fun generateSecretKey(random: SecureRandom): OreSecretKey<ClwwCiphertext> {
        return EncodeOreSecretKey(this, baseParams.generateSecretKey(random))
    }
}