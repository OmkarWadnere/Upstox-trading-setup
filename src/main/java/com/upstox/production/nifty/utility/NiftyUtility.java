package com.upstox.production.nifty.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NiftyUtility {

    public static boolean niftyCallOptionFlag = false;
    public static boolean niftyPutOptionFlag = false;
    public static int niftyMorningTradeCounter = 0;
    public static boolean isNiftyMainExecutionRunning = false;
}
