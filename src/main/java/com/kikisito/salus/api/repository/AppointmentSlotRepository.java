package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AppointmentSlotEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlotEntity, Integer> {
    List<AppointmentSlotEntity> findByDoctorAndDate(MedicalProfileEntity doctor, LocalDate date);
    List<AppointmentSlotEntity> findByDoctorAndDateBetween(MedicalProfileEntity doctor, LocalDate start, LocalDate end);
    List<AppointmentSlotEntity> findByRoomAndDate(RoomEntity room, LocalDate date);
}
