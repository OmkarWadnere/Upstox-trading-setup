package com.upstox.production.banknifty.controller;

import com.upstox.production.banknifty.dto.BankNiftyOptionMapperRequestDto;
import com.upstox.production.banknifty.service.BankNiftyNextOptionMapperService;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/banknifty")
public class BankNiftyNextOptionMapperController {

    @Autowired
    private BankNiftyNextOptionMapperService bankNiftyNextOptionMapperService;

    @PostMapping("/nextFutureMapping")
    public String addBankNiftyNextOptionMapper(@RequestBody BankNiftyOptionMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return bankNiftyNextOptionMapperService.addNextOptionMapping(futureMapperRequestDto);
    }
}
