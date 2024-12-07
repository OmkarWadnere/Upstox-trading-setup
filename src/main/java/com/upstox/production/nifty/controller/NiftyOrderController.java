package com.upstox.production.nifty.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.nifty.service.NiftyOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalTime;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.authenticatedUser;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.permissionToLoginForNonMarketHours;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradeSwitch;
import static com.upstox.production.nifty.utility.NiftyUtility.isNiftyMainExecutionRunning;

@RestController
@Slf4j
@RequestMapping("/placeOrder/nifty")
public class NiftyOrderController {

    @Autowired
    private NiftyOrderService niftyOrderService;

    @PostMapping("/tradingView")
    public void NiftyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                    @RequestBody String niftyPayload) throws UpstoxException, IOException, UnirestException, InterruptedException, URISyntaxException {
        if (tradeSwitch && authenticatedUser) {
            niftyOrderService.addOrderToQueue(niftyPayload);
        } else if (tradeSwitch && LocalTime.now().isAfter(LocalTime.of(15, 30, 1)) && permissionToLoginForNonMarketHours) {
            niftyOrderService.addOrderToQueue(niftyPayload);
        } else if (LocalTime.now().isAfter(LocalTime.of(15, 30, 01)) && !permissionToLoginForNonMarketHours) {
            throw new UpstoxException("You don't have permission to login after market hours please take permission first");
        } else {
            log.info("!!!Someone has closed trading for remaining day or you are not authenticated user to login!!!");
        }
        isNiftyMainExecutionRunning = false;
    }
}
