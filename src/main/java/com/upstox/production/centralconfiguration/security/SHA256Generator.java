package com.upstox.production.centralconfiguration.security;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.mails.ApplicationMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class SHA256Generator {

    @Autowired
    private ApplicationMailSender applicationMailSender;

    public String generateRandomSHA256() throws UpstoxException, NoSuchAlgorithmException {
        // Create a random byte array
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[32]; // 32 bytes = 256 bits
        random.nextBytes(randomBytes);

        // Create SHA-256 hash of the random bytes
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(randomBytes);

        // Convert byte array to hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String messageBody = "Newly Generated non market hour secrete code is : " + hexString.toString() + "\n Please do not share it with anyone.";
        applicationMailSender.sendMail(messageBody, "Non Market Hour Access Code");
        return hexString.toString();

    }

}
