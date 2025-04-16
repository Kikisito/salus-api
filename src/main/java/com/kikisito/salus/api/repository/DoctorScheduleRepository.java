package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.DoctorScheduleEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorScheduleEntity, Integer> {
    List<DoctorScheduleEntity> findByDoctor(MedicalProfileEntity doctor);

    List<DoctorScheduleEntity> findByDoctorAndDayOfWeek(MedicalProfileEntity doctor, DayOfWeek dayOfWeek);
}
