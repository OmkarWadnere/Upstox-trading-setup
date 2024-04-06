package com.upstox.production.centralconfiguration.service;

import com.upstox.production.centralconfiguration.dto.ExceptionalDayMapperDto;
import com.upstox.production.centralconfiguration.entity.ExceptionalDayMapper;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.ExceptionalDayMapperRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ExceptionalDayMapperService {

    @Autowired
    private ExceptionalDayMapperRepository exceptionalDayMapperRepository;

    private static List<ExceptionalDayMapper> convertIterabletoExxceptionalDayMapperList(Iterable<ExceptionalDayMapper> iterable) {
        List<ExceptionalDayMapper> list = new ArrayList<>();

        for (ExceptionalDayMapper item : iterable) {
            list.add(item);
        }

        return list;
    }

    public ExceptionalDayMapper addExceptionalDays(ExceptionalDayMapperDto exceptionalDayMapperDto) throws UpstoxException {
        Optional<ExceptionalDayMapper> optionalExceptionalDayMapper = exceptionalDayMapperRepository.findByDate(exceptionalDayMapperDto.getDate());
        if (optionalExceptionalDayMapper.isPresent()) {
            throw new UpstoxException("The provided Date is already present of data : " + optionalExceptionalDayMapper.get());
        }
        log.info("Adding the exceptional Day for : " + exceptionalDayMapperDto);
        ExceptionalDayMapper exceptionalDayMapper = ExceptionalDayMapper.builder().occasion(exceptionalDayMapperDto.getOccasion()).date(exceptionalDayMapperDto.getDate()).build();
        return exceptionalDayMapperRepository.save(exceptionalDayMapper);
    }

    public List<ExceptionalDayMapper> getAllExceptionsDays() {
        Iterable<ExceptionalDayMapper> exceptionalDayMapperIterable = exceptionalDayMapperRepository.findAll();
        log.info("Exceptional Days Data are : " + exceptionalDayMapperIterable);
        return convertIterabletoExxceptionalDayMapperList(exceptionalDayMapperIterable);
    }

    public String deleteExceptionalDay(ExceptionalDayMapperDto exceptionalDayMapperDto) {
        Optional<ExceptionalDayMapper> optionalExceptionalDayMapper = exceptionalDayMapperRepository.findByDate(exceptionalDayMapperDto.getDate());
        if (optionalExceptionalDayMapper.isPresent()) {
            log.info("Deleting the exceptional Day for : " + exceptionalDayMapperDto);
            exceptionalDayMapperRepository.delete(optionalExceptionalDayMapper.get());
            return "Data Deleted Successfully for : " + optionalExceptionalDayMapper.get();
        }
        return "There is no data for this particular data : " + exceptionalDayMapperDto;
    }

    public void deleteAllExceptionalDay() {
        exceptionalDayMapperRepository.deleteAll();
    }
}
