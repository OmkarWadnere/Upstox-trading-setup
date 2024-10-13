package com.upstox.production.nifty.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.nifty.dto.NiftyOptionMapperRequestDto;
import com.upstox.production.nifty.service.NiftyNextOptionMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/nifty")
public class NiftyNextOptionMapperController {

    @Autowired
    private NiftyNextOptionMapperService niftyNextOptionMapperService;

    @PostMapping("/nextFutureMapping")
    public String addNiftyNextOptionMapper(@RequestBody NiftyOptionMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return niftyNextOptionMapperService.addNextOptionMapping(futureMapperRequestDto);
    }
}
