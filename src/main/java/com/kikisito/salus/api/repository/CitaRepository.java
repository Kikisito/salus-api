package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.dto.CitaDTO;
import com.kikisito.salus.api.entity.CitaEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<CitaEntity, Integer> {
    List<CitaEntity> findBySlot_PerfilMedicoAndSlot_Fecha(PerfilMedicoEntity slotPerfilMedico, LocalDate slotFecha);
    List<CitaEntity> findByPaciente(UserEntity paciente);
}
