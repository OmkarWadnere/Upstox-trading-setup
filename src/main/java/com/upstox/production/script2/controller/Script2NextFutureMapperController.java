package com.upstox.production.script2.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script2.dto.Script2FutureMapperRequestDto;
import com.upstox.production.script2.service.Script2NextFutureMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/script2")
public class Script2NextFutureMapperController {

    @Autowired
    private Script2NextFutureMapperService script2NextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addBankNiftyNextFutureMapper(@RequestBody Script2FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script2NextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }
}
