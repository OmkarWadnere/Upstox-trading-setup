package com.upstox.production.centralconfiguration.schedular;

import com.upstox.production.centralconfiguration.entity.User;
import com.upstox.production.centralconfiguration.enums.UserAccess;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.mails.ApplicationMailSender;
import com.upstox.production.centralconfiguration.repository.UserRepository;
import com.upstox.production.centralconfiguration.utility.CentralUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.fetchDataUserClientId;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.fetchDataUserClientSecret;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.fetchDataUserEmailId;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradingUserClientId;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradingUserClientSecret;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradingUserEmailId;

@Component
@Slf4j
public class CentralizeScheduler {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicationMailSender applicationMailSender;

    @Scheduled(cron = "0 30 6 * * MON-FRI")
    public void everyDay5AmActivity() {
        CentralUtility.OTP = "";
        CentralUtility.otpAttempt = 0;
        CentralUtility.authenticatedUser = false;
    }

    @Scheduled(cron = "0 15 7 * * MON-FRI")
    public void userDetails() {
        try {
            Optional<User> tradeAccess = userRepository.findByUserAccessType(UserAccess.TRADE_ACCESS.getAccessType());
            Optional<User> fetchDataAccess = userRepository.findByUserAccessType(UserAccess.FETCH_DATA_ACCESS.getAccessType());
            if (tradeAccess.isEmpty() || fetchDataAccess.isEmpty()) {
                applicationMailSender.sendMail("Please check the all user details you have provided!!!", "Incorrect Users Information");
                throw new UpstoxException("Please check the all user details we have provided!!!");
            }
            tradingUserClientId = tradeAccess.get().getClientId();
            fetchDataUserClientId = fetchDataAccess.get().getClientId();
            tradingUserClientSecret = tradeAccess.get().getClientSecrete();
            fetchDataUserClientSecret = fetchDataAccess.get().getClientSecrete();
            tradingUserEmailId = tradeAccess.get().getEmailId();
            fetchDataUserEmailId = fetchDataAccess.get().getEmailId();
        } catch (Exception exception) {
            applicationMailSender.sendMail("Please check the all user details you have provided!!!", "Incorrect Users Information");
        }

    }

    @Scheduled(cron = "0 31 15 * * MON-FRI")
    public void everyDayAt1531Pm() {
        CentralUtility.authenticatedUser = false;
    }
}
