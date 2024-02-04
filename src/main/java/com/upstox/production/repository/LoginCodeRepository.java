package com.upstox.production.repository;

import com.upstox.production.entity.LoginCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginCodeRepository extends CrudRepository<LoginCode, Integer> {
}
