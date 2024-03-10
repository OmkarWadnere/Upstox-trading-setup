package com.upstox.production.script2.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script2.service.Script2OrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/placeOrder/script2")
public class Script2OrderController {

    private static final Log log = LogFactory.getLog(Script2OrderController.class);

    @Autowired
    private Script2OrderService script2OrderService;

    @PostMapping("/tradingView/buyOrder")
    public String script1BuyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                           @RequestBody String script2Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script2Payload);
        return script2OrderService.buyOrderExecution(script2Payload);
    }

    @PostMapping("/tradingView/sellOrder")
    public String script1SellOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                            @RequestBody String script2Payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + script2Payload);
        return script2OrderService.sellOrderExecution(script2Payload);
    }
}
