package com.upstox.production.script5.repository;

import com.upstox.production.script5.entity.Script5FutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script5FutureMappingRepository extends CrudRepository<Script5FutureMapping, Integer> {
    Optional<Script5FutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script5FutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<Script5FutureMapping> findBySymbolName(String symbolName);
}
