package com.upstox.production.script4.repository;

import com.upstox.production.script4.entity.Script4NextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script4NextFutureMapperRepository extends CrudRepository<Script4NextFutureMapping, Integer> {

    Optional<Script4NextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script4NextFutureMapping> findByExpiryDate(LocalDate expiryDate);

    Optional<Script4NextFutureMapping> findBySymbolName(String symbolName);

}
