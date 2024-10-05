package com.upstox.production.banknifty.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BankNiftyUtility {

    public static boolean bankNiftyCallOptionFlag = false;
    public static boolean bankNiftyPutOptionFlag = false;
    public static int bankNiftyMorningTradeCounter = 0;
    public static String schedulerToken = "";
    public static boolean isBankNiftyMainExecutionRunning = false;
}
