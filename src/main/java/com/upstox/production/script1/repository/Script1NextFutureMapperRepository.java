package com.upstox.production.script1.repository;

import com.upstox.production.script1.entity.Script1NextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script1NextFutureMapperRepository extends CrudRepository<Script1NextFutureMapping, Integer> {

    Optional<Script1NextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script1NextFutureMapping> findByExpiryDate(LocalDate expiryDate);

    Optional<Script1NextFutureMapping> findBySymbolName(String symbolName);

}
