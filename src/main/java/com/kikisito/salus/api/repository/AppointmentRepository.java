package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AppointmentEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Integer> {
    List<AppointmentEntity> findBySlot_DoctorAndSlot_Date(MedicalProfileEntity slotDoctor, LocalDate slotDate);
    List<AppointmentEntity> findByPatient(UserEntity patient);
    boolean existsBySlot_Doctor(MedicalProfileEntity doctor);
    boolean existsByIdAndSlot_Doctor_Id(Integer id, Integer slotDoctorId);
}
