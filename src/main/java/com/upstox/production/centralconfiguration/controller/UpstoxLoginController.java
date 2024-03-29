package com.upstox.production.centralconfiguration.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.service.UpstoxLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class UpstoxLoginController {

    @Autowired
    private UpstoxLoginService upstoxLoginService;

    @GetMapping("/getLoginUrl")
    @ResponseStatus(HttpStatus.OK)
    public String loggedIn() throws UnirestException {
        return upstoxLoginService.getUpstoxLoginUrl();
    }
}
