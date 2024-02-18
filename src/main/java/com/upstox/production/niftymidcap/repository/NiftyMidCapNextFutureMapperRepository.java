package com.upstox.production.niftymidcap.repository;

import com.upstox.production.niftymidcap.entity.NiftyMidCapNextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NiftyMidCapNextFutureMapperRepository extends CrudRepository<NiftyMidCapNextFutureMapping, Integer> {

    Optional<NiftyMidCapNextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<NiftyMidCapNextFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<NiftyMidCapNextFutureMapping> findBySymbolName(String symbolName);

}
