package com.upstox.production.script2.repository;

import com.upstox.production.script2.entity.Script2NextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script2NextFutureMapperRepository extends CrudRepository<Script2NextFutureMapping, Integer> {

    Optional<Script2NextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script2NextFutureMapping> findByExpiryDate(LocalDate expiryDate);

    Optional<Script2NextFutureMapping> findBySymbolName(String symbolName);

}
