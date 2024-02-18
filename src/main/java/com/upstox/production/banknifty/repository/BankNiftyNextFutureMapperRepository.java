package com.upstox.production.banknifty.repository;

import com.upstox.production.banknifty.entity.BankNiftyNextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BankNiftyNextFutureMapperRepository extends CrudRepository<BankNiftyNextFutureMapping, Integer> {

    Optional<BankNiftyNextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<BankNiftyNextFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<BankNiftyNextFutureMapping> findBySymbolName(String symbolName);

}
