package com.upstox.production.nifty.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.nifty.service.NiftyOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

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
        if (tradeSwitch) {
            niftyOrderService.addOrderToQueue(niftyPayload);
        } else {
            log.info("!!!Someone has closed trading for remaining day!!!");
        }
        isNiftyMainExecutionRunning = false;
    }
}
