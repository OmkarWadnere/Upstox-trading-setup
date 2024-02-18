package com.upstox.production.niftymidcap.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.niftymidcap.dto.NiftyMidCapFutureMapperRequestDto;
import com.upstox.production.niftymidcap.entity.NiftyMidCapFutureMapping;
import com.upstox.production.niftymidcap.entity.NiftyMidCapNextFutureMapping;
import com.upstox.production.niftymidcap.repository.NiftyMidCapFutureMappingRepository;
import com.upstox.production.niftymidcap.repository.NiftyMidCapNextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NiftyMidCapNextFutureMapperService {

    @Autowired
    private NiftyMidCapNextFutureMapperRepository niftyMidCapNextFutureMapperRepository;

    @Autowired
    private NiftyMidCapFutureMappingRepository niftyMidCapFutureMappingRepository;

    public String addNextFutureMapping(NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        NiftyMidCapNextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return niftyMidCapNextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<NiftyMidCapNextFutureMapping> optionalNextFutureMappingInstrumentToken = niftyMidCapNextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<NiftyMidCapNextFutureMapping> optionalNextFutureMappingExpiryDate = niftyMidCapNextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<NiftyMidCapFutureMapping> optionalFutureMappingInstrumentToken = niftyMidCapFutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<NiftyMidCapFutureMapping> optionalFutureMappingExpiryDate = niftyMidCapFutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public NiftyMidCapNextFutureMapping nextFutureMappingBuilder(NiftyMidCapFutureMapperRequestDto futureMapperRequestDto) {
        return NiftyMidCapNextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
