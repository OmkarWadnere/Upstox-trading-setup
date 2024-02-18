package com.upstox.production.niftymidcap.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.niftymidcap.dto.NiftyMidCapFutureMapperRequestDto;
import com.upstox.production.niftymidcap.service.NiftyMidCapFutureMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/niftymidcap")
public class NiftyMidCapFutureMappingController {

    @Autowired
    private NiftyMidCapFutureMappingService niftyMidCapFutureMappingService;

    @PostMapping("/futureMapping")
    public String addBankNiftyFutureMapping(@RequestBody NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return niftyMidCapFutureMappingService.addFutureMapping(futureMapperRequestDto);
    }
}
