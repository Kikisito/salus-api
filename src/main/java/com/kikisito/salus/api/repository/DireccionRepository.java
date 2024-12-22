package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.DireccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DireccionRepository extends JpaRepository<DireccionEntity, Integer> {
}