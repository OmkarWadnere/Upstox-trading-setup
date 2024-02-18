package com.upstox.production.idea.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.idea.dto.IdeaFutureMapperRequestDto;
import com.upstox.production.idea.entity.IdeaFutureMapping;
import com.upstox.production.idea.repository.IdeaFutureMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdeaFutureMappingService {

    @Autowired
    private IdeaFutureMappingRepository ideaFutureMappingRepository;

    public String addFutureMapping(IdeaFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        IdeaFutureMapping futureMapping = futureMappingBuilder(futureMapperRequestDto);
        return ideaFutureMappingRepository.save(futureMapping).toString();
    }

    public void validateData(IdeaFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<IdeaFutureMapping> optionalFutureMappingInstrumentToken = ideaFutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<IdeaFutureMapping> optionalFutureMappingExpiryDate = ideaFutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
    }

    public IdeaFutureMapping futureMappingBuilder(IdeaFutureMapperRequestDto futureMapperRequestDto) {
        return IdeaFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }
}
