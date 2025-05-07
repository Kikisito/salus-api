package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AppointmentEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Integer> {
    List<AppointmentEntity> findBySlot_DoctorAndSlot_Date(MedicalProfileEntity slotDoctor, LocalDate slotDate);

    List<AppointmentEntity> findByPatient(UserEntity patient);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.patient = :patient AND (a.slot.doctor = :doctor OR a.slot.specialty IN :specialties)")
    List<AppointmentEntity> findByPatientWithDoctorOrItsSpecialties(UserEntity patient, MedicalProfileEntity doctor, Collection<SpecialtyEntity> specialties);

    boolean existsBySlot_Doctor(MedicalProfileEntity doctor);

    boolean existsBySlot_DoctorAndPatient(MedicalProfileEntity doctor, UserEntity patient);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.patient = :patient AND a.slot.date >= CURRENT_DATE")
    List<AppointmentEntity> findUpcomingAppointmentsByPatient(UserEntity patient);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.patient = :patient AND a.slot.date < CURRENT_DATE")
    List<AppointmentEntity> findPastAppointmentsByPatient(UserEntity patient);

    Integer countByPatient(UserEntity patient);
}
