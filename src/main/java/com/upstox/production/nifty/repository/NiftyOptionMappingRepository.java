package com.upstox.production.nifty.repository;

import com.upstox.production.nifty.entity.NiftyOptionMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NiftyOptionMappingRepository extends CrudRepository<NiftyOptionMapping, Integer> {
    Optional<NiftyOptionMapping> findByInstrumentToken(String instrumentToken);

    Optional<NiftyOptionMapping> findByExpiryDate(LocalDate expiryDate);

    Optional<NiftyOptionMapping> findBySymbolName(String symbolName);
}
