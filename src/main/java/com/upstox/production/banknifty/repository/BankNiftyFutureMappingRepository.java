package com.upstox.production.banknifty.repository;

import com.upstox.production.banknifty.entity.BankNiftyFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BankNiftyFutureMappingRepository extends CrudRepository<BankNiftyFutureMapping, Integer> {
    Optional<BankNiftyFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<BankNiftyFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<BankNiftyFutureMapping> findBySymbolName(String symbolName);
}
