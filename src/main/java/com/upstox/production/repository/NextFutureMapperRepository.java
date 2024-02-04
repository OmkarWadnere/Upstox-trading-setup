package com.upstox.production.repository;

import com.upstox.production.entity.FutureMapping;
import com.upstox.production.entity.NextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface NextFutureMapperRepository extends CrudRepository<NextFutureMapping, Integer> {

    Optional<NextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<NextFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<NextFutureMapping> findBySymbolName(String symbolName);

}
