package com.upstox.production.centralconfiguration.service;

import com.upstox.production.centralconfiguration.dto.OtpDto;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.mails.ApplicationMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.OTP;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.OtpGenerationDateTime;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.authenticatedUser;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.otpAttempt;

@Service
public class ApplicationLoginService {

    @Autowired
    private ApplicationMailSender applicationMailSender;


    public String authenticateUser(OtpDto otpDto) throws UpstoxException {
        if (otpAttempt <= 3) {
            if (otpDto.getOtp().equals(OTP)) {
                if (!validateOtpDateTime()) {
                    return "OTP has Expired";
                }
                authenticatedUser = true;
                return "You have logged in successfully!!!";
            } else {
                otpAttempt++;
                return "Please enter valid OTP";
            }
        } else {
            String errorMsg = "You have reached maximum login attempt limit, please try again tomorrow!!!";
            applicationMailSender.sendMail(errorMsg, "Login Attempt limit Exceed");
            throw new UpstoxException(errorMsg);
        }
    }

    public boolean validateOtpDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (Math.abs(Duration.between(currentDateTime, OtpGenerationDateTime).toMinutes()) <= 15) {
            return true;
        } else {
            return false;
        }
    }
}
