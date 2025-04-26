package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.MedicalTestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalTestRepository extends JpaRepository<MedicalTestEntity, Integer> {
    boolean existsByIdAndDoctor_Id(Integer medicalTestId, Integer doctorId);
}
