package com.upstox.production.script4.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script4.dto.Script4FutureMapperRequestDto;
import com.upstox.production.script4.entity.Script4FutureMapping;
import com.upstox.production.script4.repository.Script4FutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script4FutureMappingService {

    @Autowired
    private Script4FutureMappingRepository script4FutureMappingRepository;

    public String addFutureMapping(Script4FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script4FutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return script4FutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(Script4FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script4FutureMapping> optionalFutureMappingInstrumentToken = script4FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script4FutureMapping> optionalFutureMappingExpiryDate = script4FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public Script4FutureMapping futureMappingBuilder(Script4FutureMapperRequestDto futureMapperRequestDto) {
        return Script4FutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
