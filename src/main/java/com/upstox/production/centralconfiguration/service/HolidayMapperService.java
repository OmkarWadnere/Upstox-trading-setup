package com.upstox.production.centralconfiguration.service;

import com.upstox.production.centralconfiguration.dto.ExceptionalDayMapperDto;
import com.upstox.production.centralconfiguration.dto.HolidayMapperDto;
import com.upstox.production.centralconfiguration.entity.ExceptionalDayMapper;
import com.upstox.production.centralconfiguration.entity.HolidayMapper;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.HolidayMapperRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class HolidayMapperService {

    @Autowired
    private HolidayMapperRepository holidayMapperRepository;

    public HolidayMapper addHoliday(HolidayMapperDto holidayMapperDto) throws UpstoxException {
        Optional<HolidayMapper> holidayMapperRepositoryByDate = holidayMapperRepository.findByDate(holidayMapperDto.getDate());
        if (holidayMapperRepositoryByDate.isPresent()) {
            throw new UpstoxException("The provided Date is already present of data : " + holidayMapperRepositoryByDate.get());
        }
        log.info("Adding the holiday Day for : " + holidayMapperDto);
        HolidayMapper holidayMapper = HolidayMapper.builder().occasion(holidayMapperDto.getOccasion()).date(holidayMapperDto.getDate()).build();
        return holidayMapperRepository.save(holidayMapper);
    }

    public List<HolidayMapper> getAllHolidays() {
        Iterable<HolidayMapper> holidayMapperAll = holidayMapperRepository.findAll();
        log.info("Exceptional Days Data are : " + holidayMapperAll);
        return convertIterabletoHOlidayMapperList(holidayMapperAll);
    }

    public String deleteHoliday(HolidayMapperDto holidayMapperDto) {
        Optional<HolidayMapper> holidayMapperRepositoryByDate = holidayMapperRepository.findByDate(holidayMapperDto.getDate());
        if (holidayMapperRepositoryByDate.isPresent()) {
            log.info("Deleting the Holiday for : " + holidayMapperDto);
            holidayMapperRepository.delete(holidayMapperRepositoryByDate.get());
            return "Data Deleted Successfully for : " + holidayMapperRepositoryByDate.get();
        }
        return "There is no data for this particular data : " + holidayMapperDto;
    }

    public void deleteAllHolidays() {
        holidayMapperRepository.deleteAll();
    }

    private static List<HolidayMapper> convertIterabletoHOlidayMapperList(Iterable<HolidayMapper> iterable) {
        List<HolidayMapper> list = new ArrayList<>();

        for (HolidayMapper item : iterable) {
            list.add(item);
        }

        return list;
    }
}
