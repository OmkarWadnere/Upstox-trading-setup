package com.upstox.production.banknifty.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.banknifty.service.BankNiftyOrderService;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradeSwitch;

@RestController
@Slf4j
@RequestMapping("/placeOrder/banknifty")
public class BankNiftyOrderController {

    @Autowired
    private BankNiftyOrderService bankNiftyOrderService;

    @PostMapping("/tradingView")
    public void BankNiftyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                          @RequestBody String bankNiftyPayload) throws UpstoxException, IOException, UnirestException, InterruptedException, URISyntaxException {
        if (tradeSwitch) {
            bankNiftyOrderService.buyOrderExecution(bankNiftyPayload);
        } else {
            log.info("!!!Someone has closed trading for remaining day!!!");
        }
    }
}
