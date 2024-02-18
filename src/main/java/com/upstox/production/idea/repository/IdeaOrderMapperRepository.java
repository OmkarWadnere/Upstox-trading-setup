package com.upstox.production.idea.repository;

import com.upstox.production.idea.entity.IdeaOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdeaOrderMapperRepository extends CrudRepository<IdeaOrderMapper, Integer> {

    Optional<IdeaOrderMapper> findByOrderId(String orderId);
}
