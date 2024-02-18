package com.upstox.production.idea.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.idea.service.IdeaOrderService;
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
@RequestMapping("/placeOrder/idea")
public class IdeaOrderController {

    private static final Log log = LogFactory.getLog(IdeaOrderController.class);

    @Autowired
    private IdeaOrderService ideaOrderService;

    @Async("asyncExecutor")
    @Retryable(value = { Exception.class, UpstoxException.class }, maxAttempts = 3, backoff = @Backoff(delay = 500))
    @PostMapping("/tradingView")
    public String BankNiftyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                          @RequestBody String bankNiftyPayload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + bankNiftyPayload);
        return ideaOrderService.BurOrderExecution(bankNiftyPayload);
    }
}
