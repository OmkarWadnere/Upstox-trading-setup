package com.upstox.production.centralconfiguration.controller;

import com.upstox.production.centralconfiguration.dto.HolidayMapperDto;
import com.upstox.production.centralconfiguration.entity.HolidayMapper;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.service.HolidayMapperService;
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
@RequestMapping("/holidayMapper")
@Validated
public class HolidayMapperController {

    @Autowired
    private HolidayMapperService holidayMapperService;

    @PostMapping("/add")
    public HolidayMapper addHoliday(@Valid @RequestBody HolidayMapperDto holidayMapperDto) throws UpstoxException {
        System.out.println(holidayMapperDto);
        return holidayMapperService.addHoliday(holidayMapperDto);
    }

    @DeleteMapping("/delete")
    public String deleteHoliday(@Valid @RequestBody HolidayMapperDto holidayMapperDto) {
        return holidayMapperService.deleteHoliday(holidayMapperDto);
    }

    @DeleteMapping("deleteAll")
    public void deleteAllHolidays() {
        holidayMapperService.deleteAllHolidays();
    }

    @GetMapping("/getAll")
    public List<HolidayMapper> getAllHolidays() {
        return holidayMapperService.getAllHolidays();
    }
}
