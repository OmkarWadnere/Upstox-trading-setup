package com.upstox.production.script1.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script1.dto.Script1FutureMapperRequestDto;
import com.upstox.production.script1.entity.Script1FutureMapping;
import com.upstox.production.script1.repository.Script1FutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script1FutureMappingService {

    @Autowired
    private Script1FutureMappingRepository script1FutureMappingRepository;

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
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
