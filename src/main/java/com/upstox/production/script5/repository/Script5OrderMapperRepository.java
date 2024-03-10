package com.upstox.production.script5.repository;

import com.upstox.production.script5.entity.Script5OrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Script5OrderMapperRepository extends CrudRepository<Script5OrderMapper, Integer> {

    Optional<Script5OrderMapper> findByOrderId(String orderId);
}
