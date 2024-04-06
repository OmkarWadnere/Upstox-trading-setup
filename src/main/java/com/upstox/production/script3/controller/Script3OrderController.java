package com.upstox.production.script3.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script3.service.Script3OrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/placeOrder/script3")
public class Script3OrderController {

    private static final Log log = LogFactory.getLog(Script3OrderController.class);

    @Autowired
    private Script3OrderService script3OrderService;

    @PostMapping("/tradingView/buyOrder")
    public String script1BuyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                           @RequestBody String script1Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script1Payload);
        return script3OrderService.buyOrderExecution(script1Payload);
    }

    @PostMapping("/tradingView/sellOrder")
    public String script1SellOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                            @RequestBody String script1Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script1Payload);
        return script3OrderService.sellOrderExecution(script1Payload);
    }
}
