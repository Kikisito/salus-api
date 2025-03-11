package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AusenciaMedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AusenciaMedicoRepository extends JpaRepository<AusenciaMedicoEntity, Integer> {}
