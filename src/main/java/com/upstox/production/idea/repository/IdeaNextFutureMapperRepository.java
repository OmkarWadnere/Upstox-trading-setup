package com.upstox.production.idea.repository;

import com.upstox.production.idea.entity.IdeaNextFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IdeaNextFutureMapperRepository extends CrudRepository<IdeaNextFutureMapping, Integer> {

    Optional<IdeaNextFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<IdeaNextFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<IdeaNextFutureMapping> findBySymbolName(String symbolName);

}
