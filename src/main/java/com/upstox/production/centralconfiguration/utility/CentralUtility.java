package com.upstox.production.centralconfiguration.utility;

import lombok.experimental.UtilityClass;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;


@UtilityClass
public class CentralUtility {

    public static boolean tradeSwitch = true;
    public static String atulSchedulerToken = "";
    public static String omkarSchedulerToken = "";
    public static String OTP = "";
    public static LocalDateTime OtpGenerationDateTime;
    public static int otpAttempt = 1;
    public static boolean authenticatedUser = false;
    public static boolean permissionToLoginForNonMarketHours = false;
    public static String nonMarketHourCode = "";
    public static String tradingUserClientId = "";
    public static String fetchDataUserClientId = "";
    public static String tradingUserClientSecret = "";
    public static String fetchDataUserClientSecret = "";
    public static String tradingUserEmailId = "";
    public static String fetchDataUserEmailId = "";
    public static PrivateKey privateKey;
    public static PublicKey publicKey;
}