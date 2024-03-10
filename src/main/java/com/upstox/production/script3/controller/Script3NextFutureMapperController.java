package com.upstox.production.script3.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script3.dto.Script3FutureMapperRequestDto;
import com.upstox.production.script3.service.Script3NextFutureMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/script3")
public class Script3NextFutureMapperController {

    @Autowired
    private Script3NextFutureMapperService script3NextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addScript2NextFutureMapper(@RequestBody Script3FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script3NextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }
}
