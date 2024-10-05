package com.upstox.production.banknifty.repository;

import com.upstox.production.banknifty.entity.BankNiftyOptionMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BankNiftyOptionMappingRepository extends CrudRepository<BankNiftyOptionMapping, Integer> {
    Optional<BankNiftyOptionMapping> findByInstrumentToken(String instrumentToken);

    Optional<BankNiftyOptionMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<BankNiftyOptionMapping> findBySymbolName(String symbolName);
}
