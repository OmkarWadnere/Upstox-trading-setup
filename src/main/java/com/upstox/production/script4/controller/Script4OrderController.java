package com.upstox.production.script4.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script4.service.Script4OrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/placeOrder/script1")
public class Script4OrderController {

    private static final Log log = LogFactory.getLog(Script4OrderController.class);

    @Autowired
    private Script4OrderService script4OrderService;

    @PostMapping("/tradingView/buyOrder")
    public String script1BuyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                           @RequestBody String script1Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script1Payload);
        return script4OrderService.buyOrderExecution(script1Payload);
    }

    @PostMapping("/tradingView/sellOrder")
    public String script1SellOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                            @RequestBody String script1Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script1Payload);
        return script4OrderService.sellOrderExecution(script1Payload);
    }
}
