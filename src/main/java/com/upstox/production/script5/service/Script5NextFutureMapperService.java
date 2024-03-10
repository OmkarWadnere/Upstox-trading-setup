package com.upstox.production.script5.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script5.dto.Script5FutureMapperRequestDto;
import com.upstox.production.script5.entity.Script5FutureMapping;
import com.upstox.production.script5.entity.Script5NextFutureMapping;
import com.upstox.production.script5.repository.Script5FutureMappingRepository;
import com.upstox.production.script5.repository.Script5NextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script5NextFutureMapperService {

    @Autowired
    private Script5NextFutureMapperRepository script5NextFutureMapperRepository;

    @Autowired
    private Script5FutureMappingRepository script5FutureMappingRepository;

    public String addNextFutureMapping(Script5FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script5NextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return script5NextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(Script5FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script5NextFutureMapping> optionalNextFutureMappingInstrumentToken = script5NextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script5NextFutureMapping> optionalNextFutureMappingExpiryDate = script5NextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<Script5FutureMapping> optionalFutureMappingInstrumentToken = script5FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<Script5FutureMapping> optionalFutureMappingExpiryDate = script5FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public Script5NextFutureMapping nextFutureMappingBuilder(Script5FutureMapperRequestDto futureMapperRequestDto) {
        return Script5NextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
