package com.upstox.production.banknifty.repository;

import com.upstox.production.banknifty.entity.BankNiftyOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankNiftyOrderMapperRepository extends CrudRepository<BankNiftyOrderMapper, Integer> {

    Optional<BankNiftyOrderMapper> findByOrderId(String orderId);
}
