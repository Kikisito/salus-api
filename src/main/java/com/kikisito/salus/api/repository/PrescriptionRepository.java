package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AppointmentEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.PrescriptionEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, Integer> {
    List<PrescriptionEntity> findAllByAppointment(AppointmentEntity appointment);
    List<PrescriptionEntity> findAllByDoctor(MedicalProfileEntity medicalProfile);
    List<PrescriptionEntity> findAllByPatient(UserEntity patient);
}
