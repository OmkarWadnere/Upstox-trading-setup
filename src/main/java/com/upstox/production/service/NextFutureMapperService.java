package com.upstox.production.service;

import com.upstox.production.dto.FutureMapperRequestDto;
import com.upstox.production.entity.FutureMapping;
import com.upstox.production.entity.NextFutureMapping;
import com.upstox.production.excpetion.UpstoxException;
import com.upstox.production.repository.FutureMappingRepository;
import com.upstox.production.repository.NextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NextFutureMapperService {

    @Autowired
    private NextFutureMapperRepository nextFutureMapperRepository;

    @Autowired
    private FutureMappingRepository futureMappingRepository;

    public String addNextFutureMapping(FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        NextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return nextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<NextFutureMapping> optionalNextFutureMappingInstrumentToken = nextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<NextFutureMapping> optionalNextFutureMappingExpiryDate = nextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<FutureMapping> optionalFutureMappingInstrumentToken = futureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<FutureMapping> optionalFutureMappingExpiryDate = futureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public NextFutureMapping nextFutureMappingBuilder(FutureMapperRequestDto futureMapperRequestDto) {
        return NextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
