package com.upstox.production.script1.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script1.dto.Script1FutureMapperRequestDto;
import com.upstox.production.script1.service.Script1FutureMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/script1")
public class Script1FutureMappingController {

    @Autowired
    private Script1FutureMappingService script1FutureMappingService;

    @PostMapping("/futureMapping")
    public String addBankNiftyFutureMapping(@RequestBody Script1FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script1FutureMappingService.addFutureMapping(futureMapperRequestDto);
    }
}
