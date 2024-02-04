package com.upstox.production.controller;

import com.upstox.production.dto.FutureMapperRequestDto;
import com.upstox.production.excpetion.UpstoxException;
import com.upstox.production.service.FutureMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill")
public class FutureMappingController {

    @Autowired
    private FutureMappingService futureMappingService;

    @PostMapping("/futureMapping")
    public String addFutureMapping(@RequestBody FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return futureMappingService.addFutureMapping(futureMapperRequestDto);
    }
}
