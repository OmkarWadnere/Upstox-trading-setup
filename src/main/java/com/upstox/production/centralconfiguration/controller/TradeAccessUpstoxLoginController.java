package com.upstox.production.centralconfiguration.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.service.TradeAccessUpstoxLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade/login")
public class TradeAccessUpstoxLoginController {

    @Autowired
    private TradeAccessUpstoxLoginService tradeAccessUpstoxLoginService;

    @GetMapping("/getLoginUrl")
    @ResponseStatus(HttpStatus.OK)
    public String loggedIn() throws UnirestException, UpstoxException {
        return tradeAccessUpstoxLoginService.getUpstoxLoginUrl();
    }
}
