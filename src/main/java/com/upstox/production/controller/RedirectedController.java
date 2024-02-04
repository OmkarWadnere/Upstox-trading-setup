package com.upstox.production.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.entity.UpstoxLogin;
import com.upstox.production.service.RedirectedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/redirect")
public class RedirectedController {


    @Autowired
    private RedirectedService redirectedService;

    @GetMapping("/toGetToken")
    public UpstoxLogin redirected(@RequestParam String code) throws IOException, UnirestException {
        System.out.println("code" + code);
        return redirectedService.redirctedUrl(code);
    }
}
