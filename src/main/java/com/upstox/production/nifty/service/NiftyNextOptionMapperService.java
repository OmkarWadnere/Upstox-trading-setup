package com.upstox.production.nifty.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.utility.CentralUtility;
import com.upstox.production.nifty.dto.NiftyOptionMapperRequestDto;
import com.upstox.production.nifty.entity.NiftyNextOptionMapping;
import com.upstox.production.nifty.entity.NiftyOptionMapping;
import com.upstox.production.nifty.repository.NiftyNextOptionMapperRepository;
import com.upstox.production.nifty.repository.NiftyOptionMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NiftyNextOptionMapperService {

    @Autowired
    private NiftyNextOptionMapperRepository niftyNextOptionMapperRepository;

    @Autowired
    private NiftyOptionMappingRepository niftyOptionMappingRepository;

    public String addNextOptionMapping(NiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        if (CentralUtility.authenticatedUser) {
            niftyNextOptionMapperRepository.deleteAll();
            // validate data is already present or not
            validateData(optionMapperRequestDto);

            // dto to entity conversion
            NiftyNextOptionMapping nextOptionMapping = nextOptionMappingBuilder(optionMapperRequestDto);
            return niftyNextOptionMapperRepository.save(nextOptionMapping).toString();
        } else {
            throw new UpstoxException("User is not authorized to access!!!");
        }
    }

    public void validateData(NiftyOptionMapperRequestDto optionMapperRequestDto) throws UpstoxException {
        Optional<NiftyNextOptionMapping> optionalNextOptionMappingInstrumentToken = niftyNextOptionMapperRepository.findByInstrumentToken(optionMapperRequestDto.getInstrument_token());
        Optional<NiftyNextOptionMapping> optionalNextOptionMappingExpiryDate = niftyNextOptionMapperRepository.findByExpiryDate(optionMapperRequestDto.getExpiry_date());
        if (optionalNextOptionMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<NiftyOptionMapping> optionalOptionMappingExpiryDate = niftyOptionMappingRepository.findByExpiryDate(optionMapperRequestDto.getExpiry_date());
        if (optionalOptionMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in option mapper use next expiry for next option mapping");
        }
    }

    public NiftyNextOptionMapping nextOptionMappingBuilder(NiftyOptionMapperRequestDto optionMapperRequestDto) {
        return NiftyNextOptionMapping.builder()
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
