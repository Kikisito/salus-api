package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.MedicalTestEntity;
import com.kikisito.salus.api.entity.SpecialtyEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MedicalTestRepository extends JpaRepository<MedicalTestEntity, Integer> {
    @Query("SELECT mt FROM MedicalTestEntity mt WHERE mt.patient = :patient AND (mt.doctor = :doctor OR mt.specialty IN :specialties)")
    List<MedicalTestEntity> findByPatientWithDoctorOrItsSpecialties(UserEntity patient, MedicalProfileEntity doctor, Collection<SpecialtyEntity> specialties);

    List<MedicalTestEntity> findByPatient(UserEntity patient);
}
