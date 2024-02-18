package com.upstox.production.idea.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.idea.dto.IdeaFutureMapperRequestDto;
import com.upstox.production.idea.entity.IdeaFutureMapping;
import com.upstox.production.idea.entity.IdeaNextFutureMapping;
import com.upstox.production.idea.repository.IdeaFutureMappingRepository;
import com.upstox.production.idea.repository.IdeaNextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdeaNextFutureMapperService {

    @Autowired
    private IdeaNextFutureMapperRepository ideaNextFutureMapperRepository;

    @Autowired
    private IdeaFutureMappingRepository ideaFutureMappingRepository;

    public String addNextFutureMapping(IdeaFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        IdeaNextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return ideaNextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(IdeaFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<IdeaNextFutureMapping> optionalNextFutureMappingInstrumentToken = ideaNextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<IdeaNextFutureMapping> optionalNextFutureMappingExpiryDate = ideaNextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<IdeaFutureMapping> optionalFutureMappingInstrumentToken = ideaFutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<IdeaFutureMapping> optionalFutureMappingExpiryDate = ideaFutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public IdeaNextFutureMapping nextFutureMappingBuilder(IdeaFutureMapperRequestDto futureMapperRequestDto) {
        return IdeaNextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

}
