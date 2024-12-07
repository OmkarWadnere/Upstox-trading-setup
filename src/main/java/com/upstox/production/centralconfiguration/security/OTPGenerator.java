package com.upstox.production.centralconfiguration.security;

import java.security.SecureRandom;

public class OTPGenerator {

    // Character pool: digits, uppercase, lowercase, and special characters
    private static final String CHAR_POOL =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()-_=+";
    private static final int MIN_LENGTH = 12; // Minimum length of the OTP
    private static final int MAX_LENGTH = 20; // Maximum length of the OTP

    public static String generateStrongOTP() {
        SecureRandom random = new SecureRandom();
        int otpLength = random.nextInt((MAX_LENGTH - MIN_LENGTH) + 1) + MIN_LENGTH; // Random length between 12 and 20
        StringBuilder otp = new StringBuilder(otpLength);

        for (int i = 0; i < otpLength; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            otp.append(CHAR_POOL.charAt(index));
        }

        return otp.toString();
    }
}
