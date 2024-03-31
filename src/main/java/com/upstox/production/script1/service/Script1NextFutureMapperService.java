package com.upstox.production.script1.service;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.script1.dto.Script1FutureMapperRequestDto;
import com.upstox.production.script1.entity.Script1FutureMapping;
import com.upstox.production.script1.entity.Script1NextFutureMapping;
import com.upstox.production.script1.repository.Script1FutureMappingRepository;
import com.upstox.production.script1.repository.Script1NextFutureMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class Script1NextFutureMapperService {

    @Autowired
    private Script1NextFutureMapperRepository script1NextFutureMapperRepository;

    @Autowired
    private Script1FutureMappingRepository script1FutureMappingRepository;

    public String addNextFutureMapping(Script1FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {

        // validate data is already present or not
        validateData(futureMapperRequestDto);

        // dto to entity conversion
        Script1NextFutureMapping nextFutureMapping = nextFutureMappingBuilder(futureMapperRequestDto);
        return script1NextFutureMapperRepository.save(nextFutureMapping).toString();
    }

    public void validateData(Script1FutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        Optional<Script1NextFutureMapping> optionalNextFutureMappingInstrumentToken = script1NextFutureMapperRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalNextFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists");
        }
        Optional<Script1NextFutureMapping> optionalNextFutureMappingExpiryDate = script1NextFutureMapperRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalNextFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists");
        }
        Optional<Script1FutureMapping> optionalFutureMappingInstrumentToken = script1FutureMappingRepository.findByInstrumentToken(futureMapperRequestDto.getInstrument_token());
        if (optionalFutureMappingInstrumentToken.isPresent()) {
            throw new UpstoxException("The provided instrument_token already exists in future mapper use next expiry for next future mapping");
        }
        Optional<Script1FutureMapping> optionalFutureMappingExpiryDate = script1FutureMappingRepository.findByExpiryDate(futureMapperRequestDto.getExpiry_date());
        if (optionalFutureMappingExpiryDate.isPresent()) {
            throw new UpstoxException("The provided Expiry Date already exists in future mapper use next expiry for next future mapping");
        }
    }

    public Script1NextFutureMapping nextFutureMappingBuilder(Script1FutureMapperRequestDto futureMapperRequestDto) {
        return Script1NextFutureMapping.builder()
                .instrumentToken(futureMapperRequestDto.getInstrument_token())
                .expiryDate(futureMapperRequestDto.getExpiry_date())
                .symbolName(futureMapperRequestDto.getSymbolName())
                .quantity(futureMapperRequestDto.getQuantity()).build();
    }

    public void deleteAllNextFutureMapping() {
        script1NextFutureMapperRepository.deleteAll();
    }

    public List<Script1NextFutureMapping> getAllFutureMappings() {
        Iterable<Script1NextFutureMapping> nextFutureMappings = script1NextFutureMapperRepository.findAll();
        return convertIterableToListFutureMapper(nextFutureMappings);
    }

    private static List<Script1NextFutureMapping> convertIterableToListFutureMapper(Iterable<Script1NextFutureMapping> iterable) {
        List<Script1NextFutureMapping> list = new ArrayList<>();

        for (Script1NextFutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }
}
