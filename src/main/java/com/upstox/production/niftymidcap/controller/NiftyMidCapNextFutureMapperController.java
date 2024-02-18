package com.upstox.production.niftymidcap.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.niftymidcap.dto.NiftyMidCapFutureMapperRequestDto;
import com.upstox.production.niftymidcap.service.NiftyMidCapNextFutureMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/niftymidcap")
public class NiftyMidCapNextFutureMapperController {

    @Autowired
    private NiftyMidCapNextFutureMapperService niftyMidCapNextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addBankNiftyNextFutureMapper(@RequestBody NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return niftyMidCapNextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }
}
