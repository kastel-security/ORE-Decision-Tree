package decisiontree.ore.prf;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CipherPrf implements Prf<SecretKey, byte[]> {
    private final String name;
    private final ThreadLocal<Cipher> cipher;
    private final int keySize;

    public CipherPrf(String name, int keySize) {
        this.name = name;
        this.keySize = keySize;
        cipher = ThreadLocal.withInitial(() -> {
            try {
                return Cipher.getInstance(name);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        });
        assert cipher.get() != null;
        assert generateKey(new SecureRandom()) != null;
    }

    @Override
    public byte[] evaluate(SecretKey prfKey, byte[] input) {
        try {
            var cipher = this.cipher.get();
            if (input.length > cipher.getBlockSize()) {
                throw new IllegalArgumentException();
            }
            cipher.init(Cipher.ENCRYPT_MODE, prfKey);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SecretKey generateKey(SecureRandom random) {
        KeyGenerator kg;
        try {
            kg = KeyGenerator.getInstance(name);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        kg.init(keySize, random);
        return kg.generateKey();
    }

    @Override
    public SecretKey getKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, name);
    }

    @Override
    public int getSecPar() {
        return keySize;
    }


}
