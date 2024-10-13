package com.upstox.production.nifty.repository;

import com.upstox.production.nifty.entity.NiftyOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NiftyOrderMapperRepository extends CrudRepository<NiftyOrderMapper, Integer> {

    Optional<NiftyOrderMapper> findByOrderId(String orderId);
}
