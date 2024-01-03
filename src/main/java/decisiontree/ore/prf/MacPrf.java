package decisiontree.ore.prf;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MacPrf implements Prf<SecretKey, byte[]> {
    private final ThreadLocal<Mac> mac;
    private final String name;
    private final int keySize;

    public MacPrf(String name, int keySize) {
        this.name = name;
        this.keySize = keySize;
        mac = ThreadLocal.withInitial(() -> {
            try {
                return Mac.getInstance(name);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
        assert mac.get() != null;
        assert generateKey(new SecureRandom()) != null;
    }

    @Override
    public byte[] evaluate(SecretKey prfKey, byte[] input) {
        try {
            var mac = this.mac.get();
            mac.init(prfKey);
            return mac.doFinal(input);
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
