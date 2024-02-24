package com.upstox.production.script3.repository;

import com.upstox.production.script3.entity.Script3FutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script3FutureMappingRepository extends CrudRepository<Script3FutureMapping, Integer> {
    Optional<Script3FutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script3FutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<Script3FutureMapping> findBySymbolName(String symbolName);
}
