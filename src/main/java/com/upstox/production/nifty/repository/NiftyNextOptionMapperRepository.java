package com.upstox.production.nifty.repository;

import com.upstox.production.nifty.entity.NiftyNextOptionMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NiftyNextOptionMapperRepository extends CrudRepository<NiftyNextOptionMapping, Integer> {

    Optional<NiftyNextOptionMapping> findByInstrumentToken(String instrumentToken);

    Optional<NiftyNextOptionMapping> findByExpiryDate(LocalDate expiryDate);

    Optional<NiftyNextOptionMapping> findBySymbolName(String symbolName);

}
