package com.upstox.production.centralconfiguration.repository;

import com.upstox.production.centralconfiguration.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    @Query("SELECT u from User u WHERE u.userAccessType= :userAccessType")
    Optional<User> findByUserAccessType(@Param("userAccessType") String userAccessType);

    @Query("SELECT u from User u WHERE u.clientId= :clientId")
    Optional<User> findByUserClientId(@Param("clientId") String clientId);
}
