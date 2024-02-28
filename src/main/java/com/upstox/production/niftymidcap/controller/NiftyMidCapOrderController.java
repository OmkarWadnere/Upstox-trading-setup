package com.upstox.production.niftymidcap.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.niftymidcap.service.NiftyMidCapOrderService;
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
@RequestMapping("/placeOrder/niftymidcap")
public class NiftyMidCapOrderController {

    private static final Log log = LogFactory.getLog(NiftyMidCapOrderController.class);

    @Autowired
    private NiftyMidCapOrderService niftyMidCapOrderService;

    @PostMapping("/tradingView")
    public String NiftyMidCapOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                            @RequestBody String bankNiftyPayload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + bankNiftyPayload);
        return niftyMidCapOrderService.buyOrderExecution(bankNiftyPayload);
    }
}
