package com.upstox.production.niftymidcap.repository;

import com.upstox.production.niftymidcap.entity.NiftyMidCapOrderMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NiftyMidCapOrderMapperRepository extends CrudRepository<NiftyMidCapOrderMapper, Integer> {

    Optional<NiftyMidCapOrderMapper> findByOrderId(String orderId);
}
