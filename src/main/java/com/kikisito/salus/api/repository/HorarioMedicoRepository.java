package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AgendaMedicoEntity;
import com.kikisito.salus.api.entity.MedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HorarioMedicoRepository extends JpaRepository<AgendaMedicoEntity, Integer> {}
