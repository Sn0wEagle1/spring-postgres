package com.postgresql.springlab.service;

import org.springframework.stereotype.Service;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.*;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String PRIVATE_KEY_PATH = "private_key.pem";
    private static final String PUBLIC_KEY_PATH = "public_key.pem";
    private final KeyPair keyPair;

    public CryptoService() throws Exception {
        this.keyPair = loadOrGenerateKeyPair();
    }

    private KeyPair loadOrGenerateKeyPair() throws Exception {
        if (Files.exists(Paths.get(PRIVATE_KEY_PATH)) && Files.exists(Paths.get(PUBLIC_KEY_PATH))) {
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_PATH));
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_PATH));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            return new KeyPair(publicKey, privateKey);
        }

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair newKeyPair = keyGen.generateKeyPair();

        Files.write(Paths.get(PRIVATE_KEY_PATH), newKeyPair.getPrivate().getEncoded());
        Files.write(Paths.get(PUBLIC_KEY_PATH), newKeyPair.getPublic().getEncoded());

        return newKeyPair;
    }

    public byte[] signData(String data) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(keyPair.getPrivate());
        privateSignature.update(data.getBytes());
        return privateSignature.sign();
    }

    public boolean verifySignature(String data, byte[] signatureBytes) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(keyPair.getPublic());
        publicSignature.update(data.getBytes());
        return publicSignature.verify(signatureBytes);
    }

    public String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash); // Кодируем в строку Base64
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка вычисления хэша", e);
        }
    }
}