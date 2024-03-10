package com.upstox.production.script4.repository;

import com.upstox.production.script4.entity.Script4FutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script4FutureMappingRepository extends CrudRepository<Script4FutureMapping, Integer> {
    Optional<Script4FutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script4FutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<Script4FutureMapping> findBySymbolName(String symbolName);
}
