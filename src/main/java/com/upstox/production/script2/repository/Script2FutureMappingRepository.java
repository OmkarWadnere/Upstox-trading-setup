package com.upstox.production.script2.repository;

import com.upstox.production.script2.entity.Script2FutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script2FutureMappingRepository extends CrudRepository<Script2FutureMapping, Integer> {
    Optional<Script2FutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script2FutureMapping> findByExpiryDate(LocalDate expiryDate);

    Optional<Script2FutureMapping> findBySymbolName(String symbolName);
}
