package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailAndNif(String email, String nif);

    Optional<UserEntity> findByVerificationToken(String token);

    Optional<UserEntity> findByNif(String nif);

    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nif) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%'))")
    @PreAuthorize("hasAuthority('ADMIN')")
    Page<UserEntity> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nif) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%'))")
    @PreAuthorize("hasAuthority('ADMIN')")
    int searchUsersCount(@Param("search") String search);

    boolean existsByEmail(String email);

    boolean existsByNif(String nif);
}
