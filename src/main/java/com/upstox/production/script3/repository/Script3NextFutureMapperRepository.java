package com.upstox.production.script3.repository;

import com.upstox.production.script3.entity.Script3NextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface Script3NextFutureMapperRepository extends CrudRepository<Script3NextFutureMapping, Integer> {

    Optional<Script3NextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<Script3NextFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<Script3NextFutureMapping> findBySymbolName(String symbolName);

}
