package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.CentroMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroMedicoRepository extends JpaRepository<CentroMedicoEntity, Integer> {}
