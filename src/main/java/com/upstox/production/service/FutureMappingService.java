package com.upstox.production.service;

import com.upstox.production.dto.FutureMapperRequestDto;
import com.upstox.production.entity.FutureMapping;
import com.upstox.production.excpetion.UpstoxException;
import com.upstox.production.repository.FutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FutureMappingService {

    @Autowired
    private FutureMappingRepository futureMappingRepository;

    public String addFutureMapping(FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        FutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return futureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<FutureMapping> optionalFutureMappingInstrumentToken = futureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<FutureMapping> optionalFutureMappingExpiryDate = futureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public FutureMapping futureMappingBuilder(FutureMapperRequestDto futureMapperRequestDto) {
        return FutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
