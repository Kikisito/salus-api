package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AgendaMedicoEntity;
import com.kikisito.salus.api.type.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgendaMedicoRepository extends JpaRepository<AgendaMedicoEntity, Integer> {
    Optional<AgendaMedicoEntity> findByMedico_Id(Integer medicoId);

    List<AgendaMedicoEntity> findByMedico_IdAndDiaSemana(Integer medicoId, DiaSemana diaSemana);
}
