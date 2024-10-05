package com.upstox.production.banknifty.controller;

import com.upstox.production.banknifty.dto.BankNiftyOptionMapperRequestDto;
import com.upstox.production.banknifty.service.BankNiftyOptionMappingService;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/banknifty")
public class BankNiftyOptionMappingController {

    @Autowired
    private BankNiftyOptionMappingService bankNiftyOptionMappingService;

    @PostMapping("/futureMapping")
    public String addBankNiftyOptionMapping(@RequestBody BankNiftyOptionMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return bankNiftyOptionMappingService.addOptionMapping(futureMapperRequestDto);
    }
}
