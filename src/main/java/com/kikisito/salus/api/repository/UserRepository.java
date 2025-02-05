package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailAndNif(String email, String nif);

    Optional<UserEntity> findByVerificationToken(String token);

    Optional<UserEntity> findByNif(String nif);
}
