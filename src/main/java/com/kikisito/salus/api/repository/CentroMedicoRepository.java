package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.CentroMedicoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroMedicoRepository extends JpaRepository<CentroMedicoEntity, Integer> {
    @Query("SELECT c FROM CentroMedicoEntity c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<CentroMedicoEntity> findByNombreContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
