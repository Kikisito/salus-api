package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Integer> {
    List<ReportEntity> findByAppointment(AppointmentEntity appointment);

    @Query("SELECT r FROM ReportEntity r WHERE r.patient = :patient AND (r.doctor = :doctor OR r.specialty IN :specialties)")
    List<ReportEntity> findByPatientWithDoctorOrItsSpecialties(UserEntity patient, MedicalProfileEntity doctor, Collection<SpecialtyEntity> specialties);
}
