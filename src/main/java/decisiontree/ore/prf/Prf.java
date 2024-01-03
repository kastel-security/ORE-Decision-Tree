package decisiontree.ore.prf;

import javax.crypto.SecretKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface Prf<K, V> {

    public static final Prf<SecretKey, byte[]> AES_128 = new CipherPrf("AES", 128);
    public static final Prf<SecretKey, byte[]> AES_192 = new CipherPrf("AES", 192);
    public static final Prf<SecretKey, byte[]> AES_256 = new CipherPrf("AES", 256);
    public static final Prf<SecretKey, byte[]> HMAC_SHA256 = new MacPrf("HmacSHA256", 128);
    public static final Prf<SecretKey, byte[]> HMAC_SHA384 = new MacPrf("HmacSHA384", 192);
    public static final Prf<SecretKey, byte[]> HMAC_SHA512 = new MacPrf("HmacSHA512", 256);

    V evaluate(K prfKey, byte[] input);

    K generateKey(SecureRandom random);

    K getKey(byte[] keyBytes);

    int getSecPar();

    public static Collection<String> listCiphers() {
        return listCryptoAlgorithms("Cipher");
    }

    public static Collection<String> listMacs() {
        return listCryptoAlgorithms("Mac");
    }

    public static Collection<String> listCryptoAlgorithms(String name) {
        Set<String> algs = new HashSet<>();
        for (Provider provider : Security.getProviders()) {
            provider.getServices().stream()
                    .filter(s -> name.equals(s.getType()))
                    .map(Provider.Service::getAlgorithm)
                    .forEach(algs::add);
        }
        return algs;
    }
}
