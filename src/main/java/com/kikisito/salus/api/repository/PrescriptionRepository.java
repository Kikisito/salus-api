package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, Integer> {
    List<PrescriptionEntity> findAllByAppointment(AppointmentEntity appointment);
    List<PrescriptionEntity> findAllByDoctor(MedicalProfileEntity medicalProfile);
    List<PrescriptionEntity> findAllByPatient(UserEntity patient);

    @Query("SELECT p FROM PrescriptionEntity p WHERE p.patient = :patient AND (p.doctor = :doctor OR p.specialty IN :specialties)")
    List<PrescriptionEntity> findByPatientWithDoctorOrItsSpecialties(UserEntity patient, MedicalProfileEntity doctor, Collection<SpecialtyEntity> specialties);
}
