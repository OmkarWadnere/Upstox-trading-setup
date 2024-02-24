package com.upstox.production.script3.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script3.dto.Script3FutureMapperRequestDto;
import com.upstox.production.script3.entity.Script3FutureMapping;
import com.upstox.production.script3.repository.Script3FutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script3FutureMappingService {

    @Autowired
    private Script3FutureMappingRepository script3FutureMappingRepository;

    public String addFutureMapping(Script3FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script3FutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return script3FutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(Script3FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script3FutureMapping> optionalFutureMappingInstrumentToken = script3FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script3FutureMapping> optionalFutureMappingExpiryDate = script3FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public Script3FutureMapping futureMappingBuilder(Script3FutureMapperRequestDto futureMapperRequestDto) {
        return Script3FutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
