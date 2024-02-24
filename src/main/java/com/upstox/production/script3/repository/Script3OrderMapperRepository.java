package com.upstox.production.script3.repository;

import com.upstox.production.script3.entity.Script3OrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Script3OrderMapperRepository extends CrudRepository<Script3OrderMapper, Integer> {

    Optional<Script3OrderMapper> findByOrderId(String orderId);
}
