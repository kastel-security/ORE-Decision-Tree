package decisiontree.ore.encodeore

import decisiontree.ore.OreSecretKey
import decisiontree.ore.clww.ClwwCiphertext

class EncodeOreSecretKey<K>(private val params: EncodeOreParams<K>, private val key: OreSecretKey<ClwwCiphertext>) :
    OreSecretKey<ClwwCiphertext> {
    override fun encrypt(msg: Long): ClwwCiphertext {
        return key.encrypt(params.encoding.encode(msg))
    }

    override fun decrypt(ct: ClwwCiphertext): Long {
        return decrypt(ct, 0, (1L shl params.encoding.valueLength) - 1)
    }
}