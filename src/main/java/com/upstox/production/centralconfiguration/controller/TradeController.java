package com.upstox.production.centralconfiguration.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradeSwitch;

@RestController
@RequestMapping("/controlTrade")
public class TradeController {

    @GetMapping()
    public String controlTrade() {
        tradeSwitch = false;
        return "" + tradeSwitch;
    }
}
