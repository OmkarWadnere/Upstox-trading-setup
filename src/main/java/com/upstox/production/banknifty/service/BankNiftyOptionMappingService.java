package com.upstox.production.banknifty.service;

import com.upstox.production.banknifty.dto.BankNiftyOptionMapperRequestDto;
import com.upstox.production.banknifty.entity.BankNiftyOptionMapping;
import com.upstox.production.banknifty.repository.BankNiftyOptionMappingRepository;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankNiftyOptionMappingService {

    @Autowired
    private BankNiftyOptionMappingRepository bankNiftyOptionMappingRepository;

    public String addOptionMapping(BankNiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        bankNiftyOptionMappingRepository.deleteAll();
        // validate data is already present or not
        validateData(optionMapperRequestDto);

        // dto to entity conversion
        BankNiftyOptionMapping optionMapping = optionMappingBuilder(optionMapperRequestDto);
        return bankNiftyOptionMappingRepository.save(optionMapping).toString();
    }

    public void validateData(BankNiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        Optional<BankNiftyOptionMapping> optionalOptionMappingInstrumentToken = bankNiftyOptionMappingRepository.findByInstrumentToken(optionMapperRequestDto.getInstrument_token());
        Optional<BankNiftyOptionMapping> optionalOptionMappingExpiryDate = bankNiftyOptionMappingRepository.findByExpiryDate(optionMapperRequestDto.getExpiry_date());
        if (optionalOptionMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public BankNiftyOptionMapping optionMappingBuilder(BankNiftyOptionMapperRequestDto optionMapperRequestDto) {
        return BankNiftyOptionMapping.builder()
                .instrumentToken(optionMapperRequestDto.getInstrument_token())
                .expiryDate(optionMapperRequestDto.getExpiry_date())
                .symbolName(optionMapperRequestDto.getSymbolName())
                .quantity(optionMapperRequestDto.getQuantity())
                .numberOfLots(optionMapperRequestDto.getNumberOfLots())
                .averagingPointInterval(optionMapperRequestDto.getAveragingPointInterval())
                .averagingTimes(optionMapperRequestDto.getAveragingTimes())
                .profitPoints(optionMapperRequestDto.getProfitPoints()).build();
    }
}
