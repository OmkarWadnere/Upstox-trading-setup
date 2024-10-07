package com.upstox.production.banknifty.service;

import com.upstox.production.banknifty.dto.BankNiftyOptionMapperRequestDto;
import com.upstox.production.banknifty.entity.BankNiftyOptionMapping;
import com.upstox.production.banknifty.entity.BankNiftyNextOptionMapping;
import com.upstox.production.banknifty.repository.BankNiftyOptionMappingRepository;
import com.upstox.production.banknifty.repository.BankNiftyNextOptionMapperRepository;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BankNiftyNextOptionMapperService {

    @Autowired
    private BankNiftyNextOptionMapperRepository bankNiftyNextOptionMapperRepository;

    @Autowired
    private BankNiftyOptionMappingRepository bankNiftyOptionMappingRepository;

    public String addNextOptionMapping(BankNiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        bankNiftyNextOptionMapperRepository.deleteAll();
        // validate data is already present or not
        validateData(optionMapperRequestDto);

        // dto to entity conversion
        BankNiftyNextOptionMapping nextOptionMapping = nextOptionMappingBuilder(optionMapperRequestDto);
        return bankNiftyNextOptionMapperRepository.save(nextOptionMapping).toString();
    }

    public void validateData(BankNiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        Optional<BankNiftyNextOptionMapping> optionalNextOptionMappingInstrumentToken = bankNiftyNextOptionMapperRepository.findByInstrumentToken(optionMapperRequestDto.getInstrument_token());
        Optional<BankNiftyNextOptionMapping> optionalNextOptionMappingExpiryDate = bankNiftyNextOptionMapperRepository.findByExpiryDate(optionMapperRequestDto.getExpiry_date());
        if (optionalNextOptionMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<BankNiftyOptionMapping> optionalOptionMappingExpiryDate = bankNiftyOptionMappingRepository.findByExpiryDate(optionMapperRequestDto.getExpiry_date());
        if (optionalOptionMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in option mapper use next expiry for next option mapping");
        }
    }

    public BankNiftyNextOptionMapping nextOptionMappingBuilder(BankNiftyOptionMapperRequestDto optionMapperRequestDto) {
        return BankNiftyNextOptionMapping.builder()
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
