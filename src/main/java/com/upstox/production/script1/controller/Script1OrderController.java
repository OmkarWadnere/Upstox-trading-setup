package com.upstox.production.script1.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.PlacedOrderDetails;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script1.service.Script1OrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/placeOrder/script1")
public class Script1OrderController {

    private static final Log log = LogFactory.getLog(Script1OrderController.class);

    @Autowired
    private Script1OrderService script1OrderService;

    @PostMapping("/tradingView/buyOrder")
    public PlacedOrderDetails script1BuyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                                       @RequestBody String script1Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script1Payload);
        return script1OrderService.buyOrderExecution(script1Payload);
    }

    @PostMapping("/tradingView/sellOrder")
    public PlacedOrderDetails script1SellOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                            @RequestBody String script1Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script1Payload);
        return script1OrderService.sellOrderExecution(script1Payload);
    }
}
