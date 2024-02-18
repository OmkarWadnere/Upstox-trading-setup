package com.upstox.production.centralconfiguration.repository;

import com.upstox.production.centralconfiguration.entity.LoginCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginCodeRepository extends CrudRepository<LoginCode, Integer> {
}
