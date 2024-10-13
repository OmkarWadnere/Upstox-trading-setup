package com.upstox.production.nifty.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.nifty.dto.NiftyOptionMapperRequestDto;
import com.upstox.production.nifty.entity.NiftyOptionMapping;
import com.upstox.production.nifty.repository.NiftyOptionMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NiftyOptionMappingService {

    @Autowired
    private NiftyOptionMappingRepository niftyOptionMappingRepository;

    public String addOptionMapping(NiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        niftyOptionMappingRepository.deleteAll();
        // validate data is already present or not
        validateData(optionMapperRequestDto);

        // dto to entity conversion
        NiftyOptionMapping optionMapping = optionMappingBuilder(optionMapperRequestDto);
        return niftyOptionMappingRepository.save(optionMapping).toString();
    }

    public void validateData(NiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        Optional<NiftyOptionMapping> optionalOptionMappingInstrumentToken = niftyOptionMappingRepository.findByInstrumentToken(optionMapperRequestDto.getInstrument_token());
        Optional<NiftyOptionMapping> optionalOptionMappingExpiryDate = niftyOptionMappingRepository.findByExpiryDate(optionMapperRequestDto.getExpiry_date());
        if (optionalOptionMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public NiftyOptionMapping optionMappingBuilder(NiftyOptionMapperRequestDto optionMapperRequestDto) {
        return NiftyOptionMapping.builder()
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
