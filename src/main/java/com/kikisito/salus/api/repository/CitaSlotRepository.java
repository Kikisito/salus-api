package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.dto.CitaSlotDTO;
import com.kikisito.salus.api.entity.CitaSlotEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaSlotRepository extends JpaRepository<CitaSlotEntity, Integer> {
    List<CitaSlotDTO> findByPerfilMedicoAndFecha(PerfilMedicoEntity perfilMedico, LocalDate fecha);
}
