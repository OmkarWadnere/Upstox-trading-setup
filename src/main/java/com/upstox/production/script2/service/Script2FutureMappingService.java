package com.upstox.production.script2.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script2.dto.Script2FutureMapperRequestDto;
import com.upstox.production.script2.entity.Script2FutureMapping;
import com.upstox.production.script2.repository.Script2FutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script2FutureMappingService {

    @Autowired
    private Script2FutureMappingRepository script2FutureMappingRepository;

    public String addFutureMapping(Script2FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script2FutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return script2FutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(Script2FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script2FutureMapping> optionalFutureMappingInstrumentToken = script2FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script2FutureMapping> optionalFutureMappingExpiryDate = script2FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public Script2FutureMapping futureMappingBuilder(Script2FutureMapperRequestDto futureMapperRequestDto) {
        return Script2FutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
