package com.upstox.production.centralconfiguration.scheduling;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.constants.Constants;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class CentralScheduling {

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Scheduled(cron = "0 5 9 * * ?") // Adjust the cron expression for 9:05:00 AM every morning
    public void dailyTokenSetter() throws UnirestException, IOException, UpstoxException, InterruptedException {
        log.info("Daily token setter");
        Constants.token = "Bearer " + getTokenDetails();
        log.info("Today's token : " + Constants.token);
    }

    public String getTokenDetails() throws UpstoxException {
        Optional<UpstoxLogin> optionalUpstoxLogin = upstoxLoginRepository.findByEmail(environment.getProperty("email_id"));
        UpstoxLogin upstoxLogin = optionalUpstoxLogin.orElseThrow(() -> new UpstoxException("There is no account for the mail id"));
        return upstoxLogin.getAccess_token();
    }

}
