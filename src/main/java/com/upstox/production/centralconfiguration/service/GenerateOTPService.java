package com.upstox.production.centralconfiguration.service;

import com.upstox.production.centralconfiguration.mails.ApplicationMailSender;
import com.upstox.production.centralconfiguration.security.OTPGenerator;
import com.upstox.production.centralconfiguration.utility.CentralUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.OtpGenerationDateTime;

@Service
public class GenerateOTPService {

    @Autowired
    private ApplicationMailSender applicationMailSender;

    public void generateOTP() {
        CentralUtility.OTP = OTPGenerator.generateStrongOTP();
        OtpGenerationDateTime = LocalDateTime.now();
        String mailBody = "Hi Omkar, your generated OTP to login your services is : " + CentralUtility.OTP;
        applicationMailSender.sendMail(mailBody, "Login OTP");
    }
}
