package com.upstox.production.repository;

import com.upstox.production.entity.OrderMapper;
import org.hibernate.criterion.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderMapperRepository extends CrudRepository<OrderMapper, Integer> {

    Optional<OrderMapper> findByOrderId(String orderId);
}
