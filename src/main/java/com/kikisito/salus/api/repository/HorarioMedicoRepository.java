package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AgendaMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioMedicoRepository extends JpaRepository<AgendaMedicoEntity, Integer> {}
