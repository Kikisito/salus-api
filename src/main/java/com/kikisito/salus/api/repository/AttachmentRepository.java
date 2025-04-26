package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AttachmentEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.MedicalTestEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Integer> {
    List<AttachmentEntity> findByMedicalTest(MedicalTestEntity medicalTest);
    boolean existsByIdAndUploadedBy(Integer id, UserEntity uploadedBy);
    boolean existsByIdAndMedicalTest_Patient(Integer id, UserEntity patient);
    boolean existsByIdAndMedicalTest_Doctor(Integer id, MedicalProfileEntity doctor);
}
