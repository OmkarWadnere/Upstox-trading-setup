package com.upstox.production.script1.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script1.dto.Script1FutureMapperRequestDto;
import com.upstox.production.script1.entity.Script1FutureMapping;
import com.upstox.production.script1.repository.Script1FutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class Script1FutureMappingService {

    @Autowired
    private Script1FutureMappingRepository script1FutureMappingRepository;

    private static List<Script1FutureMapping> convertIterableToListFutureMapper(Iterable<Script1FutureMapping> iterable) {
        List<Script1FutureMapping> list = new ArrayList<>();

        for (Script1FutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    public String addFutureMapping(Script1FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script1FutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return script1FutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(Script1FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script1FutureMapping> optionalFutureMappingInstrumentToken = script1FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script1FutureMapping> optionalFutureMappingExpiryDate = script1FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public Script1FutureMapping futureMappingBuilder(Script1FutureMapperRequestDto futureMapperRequestDto) {
        return Script1FutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).scriptName(futureMapperRequestDto.getScriptName()).build();
    }

    public void deleteAllFutureMapping() {
        script1FutureMappingRepository.deleteAll();
    }

    public List<Script1FutureMapping> getAllFutureMappings() {
        Iterable<Script1FutureMapping> futureMappingIterable = script1FutureMappingRepository.findAll();
        return convertIterableToListFutureMapper(futureMappingIterable);
    }
}
