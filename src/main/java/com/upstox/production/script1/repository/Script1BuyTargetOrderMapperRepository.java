package com.upstox.production.script1.repository;

import com.upstox.production.script1.entity.Script1BuyTargetOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Script1BuyTargetOrderMapperRepository extends CrudRepository<Script1BuyTargetOrderMapper, Integer> {
}
