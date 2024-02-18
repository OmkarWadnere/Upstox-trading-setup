package com.upstox.production.banknifty.service;

import com.upstox.production.banknifty.dto.BankNiftyFutureMapperRequestDto;
import com.upstox.production.banknifty.entity.BankNiftyFutureMapping;
import com.upstox.production.banknifty.entity.BankNiftyNextFutureMapping;
import com.upstox.production.banknifty.repository.BankNiftyFutureMappingRepository;
import com.upstox.production.banknifty.repository.BankNiftyNextFutureMapperRepository;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankNiftyNextFutureMapperService {

    @Autowired
    private BankNiftyNextFutureMapperRepository bankNiftyNextFutureMapperRepository;

    @Autowired
    private BankNiftyFutureMappingRepository bankNiftyFutureMappingRepository;

    public String addNextFutureMapping(BankNiftyFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        BankNiftyNextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return bankNiftyNextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(BankNiftyFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<BankNiftyNextFutureMapping> optionalNextFutureMappingInstrumentToken = bankNiftyNextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<BankNiftyNextFutureMapping> optionalNextFutureMappingExpiryDate = bankNiftyNextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<BankNiftyFutureMapping> optionalFutureMappingInstrumentToken = bankNiftyFutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<BankNiftyFutureMapping> optionalFutureMappingExpiryDate = bankNiftyFutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public BankNiftyNextFutureMapping nextFutureMappingBuilder(BankNiftyFutureMapperRequestDto futureMapperRequestDto) {
        return BankNiftyNextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
