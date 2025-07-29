package com.phegondev.auth2Peoject.repository;

import com.phegondev.auth2Peoject.model.UserListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserListEntity, Integer> {

    Optional<UserListEntity> findByEmail(String email);

//    List<UserListEntity> findByFullNameContainingIgnoreCase(String userName);
//    Optional<UserListEntity> findByUserIdAndEmail(Integer userId, String emailId);
}
