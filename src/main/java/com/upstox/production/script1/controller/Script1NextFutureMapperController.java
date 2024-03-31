package com.upstox.production.script1.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script1.dto.Script1FutureMapperRequestDto;
import com.upstox.production.script1.entity.Script1NextFutureMapping;
import com.upstox.production.script1.service.Script1NextFutureMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/fill/script1")
@Validated
public class Script1NextFutureMapperController {

    @Autowired
    private Script1NextFutureMapperService script1NextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addScript1NextFutureMapper(@Valid @RequestBody Script1FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script1NextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }

    @DeleteMapping("/deleteAllScript1/nextFutureMapping")
    public void deleteAllScript1FutureMapping() {
        script1NextFutureMapperService.deleteAllNextFutureMapping();
    }

    @GetMapping("/getAllNextFutureMapping")
    public List<Script1NextFutureMapping> getAllNextFutureMappings() {
        return script1NextFutureMapperService.getAllFutureMappings();
    }
}
