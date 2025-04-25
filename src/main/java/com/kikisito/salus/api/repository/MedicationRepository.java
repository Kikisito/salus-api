package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationRepository extends JpaRepository<MedicationEntity, Integer> {}
