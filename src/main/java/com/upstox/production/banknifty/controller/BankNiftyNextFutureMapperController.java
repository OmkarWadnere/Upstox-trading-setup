package com.upstox.production.banknifty.controller;

import com.upstox.production.banknifty.dto.BankNiftyFutureMapperRequestDto;
import com.upstox.production.banknifty.service.BankNiftyNextFutureMapperService;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/banknifty")
public class BankNiftyNextFutureMapperController {

    @Autowired
    private BankNiftyNextFutureMapperService bankNiftyNextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addBankNiftyNextFutureMapper(@RequestBody BankNiftyFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return bankNiftyNextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }
}
