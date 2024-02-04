package com.upstox.production.repository;

import com.upstox.production.entity.UpstoxLogin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpstoxLoginRepository extends CrudRepository<UpstoxLogin, Integer> {

    Optional<UpstoxLogin> findByEmail(String email);
}
