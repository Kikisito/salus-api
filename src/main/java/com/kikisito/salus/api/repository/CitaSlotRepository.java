package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.CitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<CitaEntity, Integer> {}
