package com.phegondev.auth2Peoject.repository;

import com.phegondev.auth2Peoject.model.LoginCredentialEntity;
import com.phegondev.auth2Peoject.model.UserListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginCredentialRepository extends JpaRepository<LoginCredentialEntity, Integer> {

    @Query("SELECT lc FROM LoginCredentialEntity lc " +
            "JOIN lc.user u " +
            "WHERE u.email = :emailId AND lc.password = :password")
    LoginCredentialEntity findByEmailAndPassword(String emailId, String password);

    Optional<LoginCredentialEntity> findByUser(UserListEntity userListEntity);
}
