package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilMedicoRepository extends JpaRepository<PerfilMedicoEntity, Integer> {
    boolean existsMedicoEntitiesByUser(UserEntity user);

    boolean existsMedicoEntitiesByNumeroColegiado(String numeroColegiado);

    @Query("SELECT p FROM PerfilMedicoEntity p " +
            "WHERE LOWER(p.numeroColegiado) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nif) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "EXISTS (SELECT e FROM p.especialidades e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%')))")
    @PreAuthorize("hasAuthority('ADMIN')")
    Page<PerfilMedicoEntity> search(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(p) FROM PerfilMedicoEntity p " +
            "WHERE LOWER(p.numeroColegiado) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nif) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "EXISTS (SELECT e FROM p.especialidades e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%')))")
    @PreAuthorize("hasAuthority('ADMIN')")
    int searchCount(@Param("search") String search);
}
