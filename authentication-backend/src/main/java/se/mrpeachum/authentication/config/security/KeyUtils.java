package se.mrpeachum.authentication.config.security;

import org.springframework.beans.factory.BeanInitializationException;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

class KeyUtils {

    // utility class
    private KeyUtils() {}

    static KeyPair getKeyPair(String publicKeyFileName, String privateKeyFileName) {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Bad algorithm");
        }
        try {
            return new KeyPair(getPublicKey(keyFactory, publicKeyFileName),
                getPrivateKey(keyFactory, privateKeyFileName));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate KeyPair", e);
        }
    }

    private static PrivateKey getPrivateKey(KeyFactory keyFactory, String privateKeyFileName) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(getKeyBytesOfFile(privateKeyFileName));
        return keyFactory.generatePrivate(spec);
    }

    private static PublicKey getPublicKey(KeyFactory keyFactory, String publicKeyFileName) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(getKeyBytesOfFile(publicKeyFileName));
        return keyFactory.generatePublic(spec);
    }

    private static byte[] getKeyBytesOfFile(String filename) throws Exception {
        URL resource = Optional.ofNullable(OAuth2Configuration.class.getClassLoader().getResource(filename))
                               .orElseThrow(() -> new BeanInitializationException("File not found"));
        return Files.readAllBytes(new File(resource.toURI()).toPath());
    }
}
