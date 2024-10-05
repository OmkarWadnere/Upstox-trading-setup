package com.upstox.production.banknifty.repository;

import com.upstox.production.banknifty.entity.BankNiftyNextOptionMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BankNiftyNextOptionMapperRepository extends CrudRepository<BankNiftyNextOptionMapping, Integer> {

    Optional<BankNiftyNextOptionMapping> findByInstrumentToken(String instrumentToken);

    Optional<BankNiftyNextOptionMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<BankNiftyNextOptionMapping> findBySymbolName(String symbolName);

}
