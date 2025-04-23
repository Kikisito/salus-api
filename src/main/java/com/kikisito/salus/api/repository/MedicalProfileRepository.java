package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.MedicalProfileEntity;
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
public interface MedicalProfileRepository extends JpaRepository<MedicalProfileEntity, Integer> {
    boolean existsMedicoEntitiesByUser(UserEntity user);

    boolean existsMedicoEntitiesByLicense(String license);

    @Query("SELECT p FROM MedicalProfileEntity p " +
            "WHERE LOWER(p.license) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nif) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "EXISTS (SELECT e FROM p.specialties e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    @PreAuthorize("hasAuthority('ADMIN')")
    Page<MedicalProfileEntity> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(p) FROM MedicalProfileEntity p " +
            "WHERE LOWER(p.license) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nif) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "EXISTS (SELECT e FROM p.specialties e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    @PreAuthorize("hasAuthority('ADMIN')")
    int searchCount(@Param("search") String search);

    Optional<MedicalProfileEntity> findByUser(UserEntity user);
}
