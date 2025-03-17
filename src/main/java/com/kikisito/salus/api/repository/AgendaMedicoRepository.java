package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AgendaMedicoEntity;
import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AgendaMedicoRepository extends JpaRepository<AgendaMedicoEntity, Integer> {
    List<AgendaMedicoEntity> findByMedico(PerfilMedicoEntity medico);

    List<AgendaMedicoEntity> findByMedicoAndDiaSemana(PerfilMedicoEntity medico, DayOfWeek diaSemana);
}
