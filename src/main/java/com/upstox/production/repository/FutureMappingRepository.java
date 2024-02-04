package com.upstox.production.repository;

import com.upstox.production.entity.FutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FutureMappingRepository extends CrudRepository<FutureMapping, Integer> {
    Optional<FutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<FutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<FutureMapping> findBySymbolName(String symbolName);
}
