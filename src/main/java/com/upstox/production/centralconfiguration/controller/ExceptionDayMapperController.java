package com.upstox.production.centralconfiguration.controller;

import com.upstox.production.centralconfiguration.dto.ExceptionalDayMapperDto;
import com.upstox.production.centralconfiguration.entity.ExceptionalDayMapper;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.service.ExceptionalDayMapperService;
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
@RequestMapping("/exceptionalDay")
@Validated
public class ExceptionDayMapperController {

    @Autowired
    private ExceptionalDayMapperService exceptionalDayMapperService;

    @PostMapping("/add")
    public ExceptionalDayMapper addExceptionDays(@Valid @RequestBody ExceptionalDayMapperDto exceptionalDayMapperDto) throws UpstoxException {
        return exceptionalDayMapperService.addExceptionalDays(exceptionalDayMapperDto);
    }

    @DeleteMapping("/delete")
    public String deleteExceptionDay(@Valid @RequestBody ExceptionalDayMapperDto exceptionalDayMapperDto) {
        return exceptionalDayMapperService.deleteExceptionalDay(exceptionalDayMapperDto);
    }

    @GetMapping("/getAll")
    public List<ExceptionalDayMapper> getAllExceptionsDays() {
        return exceptionalDayMapperService.getAllExceptionsDays();
    }

    @DeleteMapping("/deleteAll")
    public void deleteAllExceptionDay() {
        exceptionalDayMapperService.deleteAllExceptionalDay();
    }
}
