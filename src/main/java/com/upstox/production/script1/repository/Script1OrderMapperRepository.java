package com.upstox.production.script1.repository;

import com.upstox.production.script1.entity.Script1OrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Script1OrderMapperRepository extends CrudRepository<Script1OrderMapper, Integer> {

    Optional<Script1OrderMapper> findByOrderId(String orderId);
}
