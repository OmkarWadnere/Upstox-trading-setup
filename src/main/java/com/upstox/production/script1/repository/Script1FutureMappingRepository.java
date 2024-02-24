package com.upstox.production.script1.repository;

import com.upstox.production.script1.entity.Script1FutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script1FutureMappingRepository extends CrudRepository<Script1FutureMapping, Integer> {
    Optional<Script1FutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script1FutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<Script1FutureMapping> findBySymbolName(String symbolName);
}
