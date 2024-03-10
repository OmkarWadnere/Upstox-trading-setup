package com.upstox.production.script5.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script5.dto.Script5FutureMapperRequestDto;
import com.upstox.production.script5.service.Script5NextFutureMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/script5")
public class Script5NextFutureMapperController {

    @Autowired
    private Script5NextFutureMapperService script5NextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addScript5NextFutureMapper(@RequestBody Script5FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script5NextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }
}
