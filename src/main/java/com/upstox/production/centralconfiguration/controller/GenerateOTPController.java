package com.upstox.production.centralconfiguration.controller;

import com.upstox.production.centralconfiguration.service.GenerateOTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generateOTP")
public class GenerateOTPController {

    @Autowired
    private GenerateOTPService generateOTPService;

    @GetMapping("/getOtp")
    public void generateOTP() {
        generateOTPService.generateOTP();
    }
}
