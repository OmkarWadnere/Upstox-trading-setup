package com.upstox.production.niftymidcap.repository;

import com.upstox.production.niftymidcap.entity.NiftyMidCapFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NiftyMidCapFutureMappingRepository extends CrudRepository<NiftyMidCapFutureMapping, Integer> {
    Optional<NiftyMidCapFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<NiftyMidCapFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<NiftyMidCapFutureMapping> findBySymbolName(String symbolName);
}
