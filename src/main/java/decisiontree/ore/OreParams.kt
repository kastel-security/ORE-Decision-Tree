package decisiontree.ore

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom

interface OreParams<T : Comparable<T>> {
    fun generateSecretKey(random: SecureRandom = SecureRandom()): OreSecretKey<T>
}