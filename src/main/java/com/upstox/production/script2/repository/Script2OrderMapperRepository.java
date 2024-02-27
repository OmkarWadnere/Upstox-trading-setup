package com.upstox.production.script2.repository;

import com.upstox.production.script2.entity.Script2OrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Script2OrderMapperRepository extends CrudRepository<Script2OrderMapper, Integer> {

    Optional<Script2OrderMapper> findByOrderId(String orderId);
}
