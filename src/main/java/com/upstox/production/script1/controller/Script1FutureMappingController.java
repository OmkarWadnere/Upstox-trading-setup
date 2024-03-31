package com.upstox.production.script1.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script1.dto.Script1FutureMapperRequestDto;
import com.upstox.production.script1.entity.Script1FutureMapping;
import com.upstox.production.script1.service.Script1FutureMappingService;
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
public class Script1FutureMappingController {

    @Autowired
    private Script1FutureMappingService script1FutureMappingService;

    @PostMapping("/futureMapping")
    public String addScript1FutureMapping(@Valid @RequestBody Script1FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return script1FutureMappingService.addFutureMapping(futureMapperRequestDto);
    }

    @DeleteMapping("z")
    public void deleteAllScript1FutureMapping() {
        script1FutureMappingService.deleteAllFutureMapping();
    }

    @GetMapping("/getAllFutureMapping")
    public List<Script1FutureMapping> getAllFutureMappings() {
        return script1FutureMappingService.getAllFutureMappings();
    }
}
