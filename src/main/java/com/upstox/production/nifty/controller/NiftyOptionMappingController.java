package com.upstox.production.nifty.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.nifty.dto.NiftyOptionMapperRequestDto;
import com.upstox.production.nifty.service.NiftyOptionMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/nifty")
public class NiftyOptionMappingController {

    @Autowired
    private NiftyOptionMappingService niftyOptionMappingService;

    @PostMapping("/futureMapping")
    public String addNiftyOptionMapping(@RequestBody NiftyOptionMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return niftyOptionMappingService.addOptionMapping(futureMapperRequestDto);
    }
}
