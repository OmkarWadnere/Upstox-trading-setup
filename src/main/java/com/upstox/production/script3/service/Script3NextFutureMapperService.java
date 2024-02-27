package com.upstox.production.script3.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script3.dto.Script3FutureMapperRequestDto;
import com.upstox.production.script3.entity.Script3FutureMapping;
import com.upstox.production.script3.entity.Script3NextFutureMapping;
import com.upstox.production.script3.repository.Script3FutureMappingRepository;
import com.upstox.production.script3.repository.Script3NextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Script3NextFutureMapperService {

    @Autowired
    private Script3NextFutureMapperRepository script3NextFutureMapperRepository;

    @Autowired
    private Script3FutureMappingRepository script3FutureMappingRepository;

    public String addNextFutureMapping(Script3FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script3NextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return script3NextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(Script3FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script3NextFutureMapping> optionalNextFutureMappingInstrumentToken = script3NextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script3NextFutureMapping> optionalNextFutureMappingExpiryDate = script3NextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<Script3FutureMapping> optionalFutureMappingInstrumentToken = script3FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<Script3FutureMapping> optionalFutureMappingExpiryDate = script3FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public Script3NextFutureMapping nextFutureMappingBuilder(Script3FutureMapperRequestDto futureMapperRequestDto) {
        return Script3NextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
