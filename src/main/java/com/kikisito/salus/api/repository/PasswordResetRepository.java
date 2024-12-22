package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.PasswordResetEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetEntity, Integer> {
    Optional<PasswordResetEntity> findByToken(String token);

    Optional<PasswordResetEntity> findByUserEntity(UserEntity userEntity);
}
