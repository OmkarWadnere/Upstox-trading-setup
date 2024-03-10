package com.upstox.production.script4.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script4.dto.Script4FutureMapperRequestDto;
import com.upstox.production.script4.service.Script4FutureMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/script1")
public class Script4FutureMappingController {

    @Autowired
    private Script4FutureMappingService script4FutureMappingService;

    @PostMapping("/futureMapping")
    public String addScript1FutureMapping(@RequestBody Script4FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script4FutureMappingService.addFutureMapping(futureMapperRequestDto);
    }
}
