package com.upstox.production.centralconfiguration.controller;

import com.upstox.production.centralconfiguration.dto.NonMarketHourDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.nonMarketHourCode;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.permissionToLoginForNonMarketHours;

@RestController
@RequestMapping("/nonMarketHour")
public class NonMarketHoursPermission {

    @PostMapping("/permission")
    public String nonMarketHourPermission(@RequestBody NonMarketHourDto nonMarketHourDto) {
        if (nonMarketHourDto.getSecretCode().equals(nonMarketHourCode)) {
            permissionToLoginForNonMarketHours = true;
            return "Now you are able to do trade after non market hours";
        } else {
            return "Please enter valid code";
        }
    }
}
