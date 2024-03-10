package com.upstox.production.script5.repository;

import com.upstox.production.script5.entity.Script5NextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script5NextFutureMapperRepository extends CrudRepository<Script5NextFutureMapping, Integer> {

    Optional<Script5NextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script5NextFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<Script5NextFutureMapping> findBySymbolName(String symbolName);

}
