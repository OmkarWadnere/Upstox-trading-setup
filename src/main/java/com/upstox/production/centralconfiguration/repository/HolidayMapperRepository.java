package com.upstox.production.centralconfiguration.repository;

import com.upstox.production.centralconfiguration.entity.ExceptionalDayMapper;
import com.upstox.production.centralconfiguration.entity.HolidayMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HolidayMapperRepository extends CrudRepository<HolidayMapper, Integer> {

    Optional<HolidayMapper> findByOccasion(String occasion);
    Optional<HolidayMapper> findByDate(LocalDate date);
}
