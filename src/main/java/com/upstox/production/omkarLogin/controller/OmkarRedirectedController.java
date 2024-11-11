package com.upstox.production.omkarLogin.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.omkarLogin.service.OmkarRedirectedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/omkar/redirect")
public class OmkarRedirectedController {

    @Autowired
    private OmkarRedirectedService omkarRedirectedService;

    @GetMapping("/toGetToken")
    public UpstoxLogin redirected(@RequestParam String code) throws IOException, UnirestException {
        return omkarRedirectedService.redirctedUrl(code);
    }
}
