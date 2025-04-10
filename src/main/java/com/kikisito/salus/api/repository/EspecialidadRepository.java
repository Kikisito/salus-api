package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.EspecialidadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadRepository extends JpaRepository<EspecialidadEntity, Integer> {
    Page<EspecialidadEntity> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
