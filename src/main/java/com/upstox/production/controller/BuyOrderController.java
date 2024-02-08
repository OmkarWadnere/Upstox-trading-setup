package com.upstox.production.controller;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.excpetion.UpstoxException;
import com.upstox.production.service.BuyOrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/placeBuyOrder")
public class BuyOrderController {

    private static final Log log = LogFactory.getLog(BuyOrderController.class);

    @Autowired
    private BuyOrderService buyOrderService;

    @PostMapping("/tradingView")
    public String BuyOrderExecution(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                              @RequestBody String payload) throws UpstoxException, IOException, UnirestException, InterruptedException {
        log.info("Data received to place order is : " + payload);
        return buyOrderService.BurOrderExecution(payload);
    }
}
