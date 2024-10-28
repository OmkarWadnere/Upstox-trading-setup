package com.upstox.production.nifty.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NiftyUtility {

    public static boolean niftyCallOptionFlag = false;
    public static boolean niftyPutOptionFlag = false;
    public static int niftyMorningTradeCounter = 0;
    public static boolean isNiftyMainExecutionRunning = false;
    public static boolean trailSlOrderRunning = false;
    public static boolean ltpPriceFetching = false;
    public static double niftyOptionLtp = 0.00;
    public static double niftyCurrentOptionTradeHigh = 0.00;
    public static double niftyOptionBuyPrice = 0.00;
    public static double niftyOptionInitialTargetPrice = 0.00;
    public static double niftyOptionHighPrice = 0.00;
    public static double niftyTrailSlPrice = 0.00;
    public static int niftyRemainingQuantity = 0;
    public static String niftyCurrentInstrument = "";
    public static boolean niftyTargetTradeStatus = false;

}
