package com.upstox.production.centralconfiguration.mails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationMailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendMail(String mailBody, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("omkarsskpatil@gmail.com");
        message.setSubject(subject);
        message.setText(mailBody);
        message.setFrom("omkar.wadnere0779@gmail.com");
        mailSender.send(message);
        log.info("Mail send successfully for : " + subject);
    }
}
