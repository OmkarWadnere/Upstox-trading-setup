package com.upstox.production.script2.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script2.dto.Script2FutureMapperRequestDto;
import com.upstox.production.script2.service.Script2FutureMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/script2")
public class Script2FutureMappingController {

    @Autowired
    private Script2FutureMappingService script2FutureMappingService;

    @PostMapping("/futureMapping")
    public String addBankNiftyFutureMapping(@RequestBody Script2FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script2FutureMappingService.addFutureMapping(futureMapperRequestDto);
    }
}
