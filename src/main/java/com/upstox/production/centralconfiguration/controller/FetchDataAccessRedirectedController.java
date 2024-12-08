package com.upstox.production.centralconfiguration.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.service.FetchDataAccessRedirectedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/fetch/redirect")
public class FetchDataAccessRedirectedController {

    @Autowired
    private FetchDataAccessRedirectedService fetchDataAccessRedirectedService;

    @GetMapping("/toGetToken")
    public UpstoxLogin redirected(@RequestParam String code) throws IOException, UnirestException, UpstoxException {
        return fetchDataAccessRedirectedService.redirctedUrl(code);
    }
}
