package com.upstox.test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/myLogin")
public class Login {

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public String loggedIn() throws UnirestException {
        HttpResponse response1 = Unirest.get("https://api.upstox.com/v2/login/authorization/dialog?client_id=e2809017-3a4e-4821-97ca-c21c63cd6082&redirect_uri=http://localhost:8080/redirect/here")
                .asString();
        return "https://api.upstox.com/v2/login/authorization/dialog?client_id=e2809017-3a4e-4821-97ca-c21c63cd6082&redirect_uri=http://localhost:8080/redirect/here";
    }
}