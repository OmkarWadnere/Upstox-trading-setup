package com.upstox.production.centralconfiguration.repository;

import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpstoxLoginRepository extends CrudRepository<UpstoxLogin, Integer> {

    Optional<UpstoxLogin> findByEmail(String email);
}
