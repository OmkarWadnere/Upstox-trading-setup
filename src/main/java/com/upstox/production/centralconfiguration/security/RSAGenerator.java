package com.upstox.production.centralconfiguration.security;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAGenerator {

    // Method to generate a public/private key pair
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);  // Strong 2048-bit key size
        return keyPairGenerator.generateKeyPair();
    }

    // Method to encrypt plaintext using the public key
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  // Use PKCS1Padding or OAEP
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);  // Encode to Base64 for transmission
    }

    // Method to decrypt ciphertext using the private key
    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");  // Ensure same padding as encryption
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);  // Decode from Base64
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, "UTF-8");  // Decode to string
    }

    // Method to save private key to a file
    public static void savePrivateKey(PrivateKey privateKey, String fileName) throws Exception {
        try (ObjectOutputStream keyOut = new ObjectOutputStream(new FileOutputStream(fileName))) {
            keyOut.writeObject(privateKey);
        }
    }

    // Method to save public key to a file
    public static void savePublicKey(PublicKey publicKey, String fileName) throws Exception {
        try (ObjectOutputStream keyOut = new ObjectOutputStream(new FileOutputStream(fileName))) {
            keyOut.writeObject(publicKey);
        }
    }

    // Method to load private key from a file
    public static PrivateKey loadPrivateKey(String fileName) throws Exception {
        try (ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(fileName))) {
            return (PrivateKey) keyIn.readObject();
        }
    }

    // Method to load public key from a file
    public static PublicKey loadPublicKey(String fileName) throws Exception {
        try (ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(fileName))) {
            return (PublicKey) keyIn.readObject();
        }
    }
}
