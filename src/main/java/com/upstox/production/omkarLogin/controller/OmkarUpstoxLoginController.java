package com.upstox.production.omkarLogin.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.omkarLogin.service.OmkarUpstoxLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/omkar/login")
public class OmkarUpstoxLoginController {

    @Autowired
    private OmkarUpstoxLoginService omkarUpstoxLoginService;

    @GetMapping("/getLoginUrl")
    @ResponseStatus(HttpStatus.OK)
    public String loggedIn() throws UnirestException {
        return omkarUpstoxLoginService.getUpstoxLoginUrl();
    }
}