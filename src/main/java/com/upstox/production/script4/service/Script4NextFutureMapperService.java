package com.upstox.production.script4.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script4.dto.Script4FutureMapperRequestDto;
import com.upstox.production.script4.entity.Script4FutureMapping;
import com.upstox.production.script4.entity.Script4NextFutureMapping;
import com.upstox.production.script4.repository.Script4FutureMappingRepository;
import com.upstox.production.script4.repository.Script4NextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script4NextFutureMapperService {

    @Autowired
    private Script4NextFutureMapperRepository script4NextFutureMapperRepository;

    @Autowired
    private Script4FutureMappingRepository script4FutureMappingRepository;

    public String addNextFutureMapping(Script4FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script4NextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return script4NextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(Script4FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script4NextFutureMapping> optionalNextFutureMappingInstrumentToken = script4NextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script4NextFutureMapping> optionalNextFutureMappingExpiryDate = script4NextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<Script4FutureMapping> optionalFutureMappingInstrumentToken = script4FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<Script4FutureMapping> optionalFutureMappingExpiryDate = script4FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public Script4NextFutureMapping nextFutureMappingBuilder(Script4FutureMapperRequestDto futureMapperRequestDto) {
        return Script4NextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
