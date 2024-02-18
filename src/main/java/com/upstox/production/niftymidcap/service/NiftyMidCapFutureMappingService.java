package com.upstox.production.niftymidcap.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.niftymidcap.dto.NiftyMidCapFutureMapperRequestDto;
import com.upstox.production.niftymidcap.entity.NiftyMidCapFutureMapping;
import com.upstox.production.niftymidcap.repository.NiftyMidCapFutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NiftyMidCapFutureMappingService {

    @Autowired
    private NiftyMidCapFutureMappingRepository niftyMidCapFutureMappingRepository;

    public String addFutureMapping(NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        NiftyMidCapFutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return niftyMidCapFutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<NiftyMidCapFutureMapping> optionalFutureMappingInstrumentToken = niftyMidCapFutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<NiftyMidCapFutureMapping> optionalFutureMappingExpiryDate = niftyMidCapFutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public NiftyMidCapFutureMapping futureMappingBuilder(NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) {
        return NiftyMidCapFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
