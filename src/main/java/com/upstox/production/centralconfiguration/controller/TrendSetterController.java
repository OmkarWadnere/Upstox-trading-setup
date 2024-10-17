package com.upstox.production.centralconfiguration.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.*;

@RestController
public class TrendSetterController {

    @GetMapping("/bullish")
    public String setBullishTrend() {
        niftyBullish = true;
        niftyNeutral = false;
        niftyBearish = false;
        return "Bullish Trend set as : " + niftyBullish + ". Bearish Trend set as : " + niftyBearish + ". Neutral Trend set as : " + niftyNeutral;
    }

    @GetMapping("/bearish")
    public String setBearishTrend() {
        niftyBullish = false;
        niftyNeutral = false;
        niftyBearish = true;
        return "Bullish Trend set as : " + niftyBullish + ". Bearish Trend set as : " + niftyBearish + ". Neutral Trend set as : " + niftyNeutral;
    }

    @GetMapping("/neutral")
    public String setNeutralTrend() {
        niftyBullish = false;
        niftyNeutral = true;
        niftyBearish = false;
        return "Bullish Trend set as : " + niftyBullish + ". Bearish Trend set as : " + niftyBearish + ". Neutral Trend set as : " + niftyNeutral;
    }
}
