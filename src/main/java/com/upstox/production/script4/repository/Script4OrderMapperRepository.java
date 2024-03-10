package com.upstox.production.script4.repository;

import com.upstox.production.script4.entity.Script4OrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Script4OrderMapperRepository extends CrudRepository<Script4OrderMapper, Integer> {

    Optional<Script4OrderMapper> findByOrderId(String orderId);
}
