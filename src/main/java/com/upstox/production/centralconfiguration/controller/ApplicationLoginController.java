package com.upstox.production.centralconfiguration.controller;

import com.upstox.production.centralconfiguration.dto.OtpDto;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.service.ApplicationLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class ApplicationLoginController {

    @Autowired
    private ApplicationLoginService applicationLoginService;

    @PostMapping
    public String authenticateUser(@RequestBody OtpDto otp) throws UpstoxException {
        return applicationLoginService.authenticateUser(otp);
    }
}
