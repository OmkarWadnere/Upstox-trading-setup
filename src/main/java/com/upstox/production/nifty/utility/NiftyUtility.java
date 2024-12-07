package com.upstox.production.nifty.utility;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class NiftyUtility {

    public static boolean isNiftyMainExecutionRunning = false;
    public static double niftyOptionBuyPrice = 0.00;
    public static double niftySlPrice = 0.00;
    public static String niftyCurrentInstrumentToken = "";
    public static double maxDrawDown = 0.00;
    public static LocalDate currentTradeExpiryDate = LocalDate.of(2000, 01, 01);
    public static String currentTradeType = "";
    public static String currentParentInstrument;

}
