package com.upstox.production.script1.repository;

import com.upstox.production.script1.entity.Script1TargetOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Script1TargetOrderMapperRepository extends CrudRepository<Script1TargetOrderMapper, Integer> {
}
