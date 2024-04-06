package com.upstox.production.centralconfiguration.repository;

import com.upstox.production.centralconfiguration.entity.ExceptionalDayMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExceptionalDayMapperRepository extends CrudRepository<ExceptionalDayMapper, Integer> {

    Optional<ExceptionalDayMapper> findByOccasion(String occasion);

    Optional<ExceptionalDayMapper> findByDate(LocalDate date);
}
