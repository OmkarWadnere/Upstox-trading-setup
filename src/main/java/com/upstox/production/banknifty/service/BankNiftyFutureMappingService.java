package com.upstox.production.banknifty.service;

import com.upstox.production.banknifty.dto.BankNiftyFutureMapperRequestDto;
import com.upstox.production.banknifty.entity.BankNiftyFutureMapping;
import com.upstox.production.banknifty.repository.BankNiftyFutureMappingRepository;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankNiftyFutureMappingService {

    @Autowired
    private BankNiftyFutureMappingRepository bankNiftyFutureMappingRepository;

    public String addFutureMapping(BankNiftyFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        BankNiftyFutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return bankNiftyFutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(BankNiftyFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<BankNiftyFutureMapping> optionalFutureMappingInstrumentToken = bankNiftyFutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<BankNiftyFutureMapping> optionalFutureMappingExpiryDate = bankNiftyFutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public BankNiftyFutureMapping futureMappingBuilder(BankNiftyFutureMapperRequestDto futureMapperRequestDto) {
        return BankNiftyFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
