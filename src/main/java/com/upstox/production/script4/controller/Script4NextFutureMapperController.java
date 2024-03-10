package com.upstox.production.script4.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script4.dto.Script4FutureMapperRequestDto;
import com.upstox.production.script4.service.Script4NextFutureMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/script1")
public class Script4NextFutureMapperController {

    @Autowired
    private Script4NextFutureMapperService script4NextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addScript1NextFutureMapper(@RequestBody Script4FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script4NextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }
}
