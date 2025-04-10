package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.ConsultaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Integer> {
    Page<ConsultaEntity> findByNombreContainingIgnoreCaseOrCentroMedico_NombreContainingIgnoreCase(String nombre, String centroMedico, Pageable pageable);
}
