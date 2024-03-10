package com.upstox.production.script5.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script5.dto.Script5FutureMapperRequestDto;
import com.upstox.production.script5.entity.Script5FutureMapping;
import com.upstox.production.script5.repository.Script5FutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script5FutureMappingService {

    @Autowired
    private Script5FutureMappingRepository script5FutureMappingRepository;

    public String addFutureMapping(Script5FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script5FutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return script5FutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(Script5FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script5FutureMapping> optionalFutureMappingInstrumentToken = script5FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script5FutureMapping> optionalFutureMappingExpiryDate = script5FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public Script5FutureMapping futureMappingBuilder(Script5FutureMapperRequestDto futureMapperRequestDto) {
        return Script5FutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
