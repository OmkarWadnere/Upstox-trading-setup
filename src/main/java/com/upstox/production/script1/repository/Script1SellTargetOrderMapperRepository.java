package com.upstox.production.script1.repository;

import com.upstox.production.script1.entity.Script1BuyTargetOrderMapper;
import com.upstox.production.script1.entity.Script1SellTargetOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Script1SellTargetOrderMapperRepository extends CrudRepository<Script1SellTargetOrderMapper, Integer> {
}
