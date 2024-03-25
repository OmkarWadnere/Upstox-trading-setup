package com.upstox.production.script1.repository;

import com.upstox.production.script1.entity.Script1ScheduleOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Script1ScheduleOrderMapperRepository extends CrudRepository<Script1ScheduleOrderMapper, Integer> {
}
