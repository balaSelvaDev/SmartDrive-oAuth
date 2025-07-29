package com.phegondev.auth2Peoject.repository;

import com.phegondev.auth2Peoject.model.GoogleUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleUserRepository extends JpaRepository<GoogleUserEntity, String> {

    // Optional: Find by email
    GoogleUserEntity findByEmail(String email);

}