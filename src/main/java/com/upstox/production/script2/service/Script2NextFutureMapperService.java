package com.upstox.production.script2.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script2.dto.Script2FutureMapperRequestDto;
import com.upstox.production.script2.entity.Script2FutureMapping;
import com.upstox.production.script2.entity.Script2NextFutureMapping;
import com.upstox.production.script2.repository.Script2FutureMappingRepository;
import com.upstox.production.script2.repository.Script2NextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script2NextFutureMapperService {

    @Autowired
    private Script2NextFutureMapperRepository script2NextFutureMapperRepository;

    @Autowired
    private Script2FutureMappingRepository script2FutureMappingRepository;

    public String addNextFutureMapping(Script2FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script2NextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return script2NextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(Script2FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script2NextFutureMapping> optionalNextFutureMappingInstrumentToken = script2NextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script2NextFutureMapping> optionalNextFutureMappingExpiryDate = script2NextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<Script2FutureMapping> optionalFutureMappingInstrumentToken = script2FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<Script2FutureMapping> optionalFutureMappingExpiryDate = script2FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public Script2NextFutureMapping nextFutureMappingBuilder(Script2FutureMapperRequestDto futureMapperRequestDto) {
        return Script2NextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
