package com.upstox.production.script3.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script3.service.Script3OrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/placeOrder/script3")
public class Script3OrderController {

    private static final Log log = LogFactory.getLog(Script3OrderController.class);

    @Autowired
    private Script3OrderService script3OrderService;

    @Async("asyncExecutor")
    @Retryable(value = { Exception.class, UpstoxException.class }, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @PostMapping("/tradingView")
    public String BankNiftyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                          @RequestBody String bankNiftyPayload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + bankNiftyPayload);
        return script3OrderService.buyOrderExecution(bankNiftyPayload);
    }
}
