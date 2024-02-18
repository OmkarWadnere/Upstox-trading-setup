package com.upstox.production.idea.repository;

import com.upstox.production.idea.entity.IdeaFutureMapping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IdeaFutureMappingRepository extends CrudRepository<IdeaFutureMapping, Integer> {
    Optional<IdeaFutureMapping> findByInstrumentToken(String instrumentToken);

    Optional<IdeaFutureMapping> findByExpiryDate(LocalDate expiryDate);
    Optional<IdeaFutureMapping> findBySymbolName(String symbolName);
}
