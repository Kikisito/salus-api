package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlotEntity, Integer> {
    List<AppointmentSlotEntity> findByDoctorAndDate(MedicalProfileEntity doctor, LocalDate date);
    List<AppointmentSlotEntity> findByDoctorAndDateBetween(MedicalProfileEntity doctor, LocalDate start, LocalDate end);
    List<AppointmentSlotEntity> findByRoomAndDate(RoomEntity room, LocalDate date);

    @Query("""
            SELECT slots FROM AppointmentSlotEntity slots
            JOIN RoomEntity r ON slots.room = r
            JOIN MedicalCenterEntity mc ON r.medicalCenter = mc
            WHERE mc = :medicalCenter
            AND slots.specialty = :specialty
                AND slots.doctor = :doctor
                AND (slots.date > CURRENT_DATE OR (slots.date = CURRENT_DATE AND slots.startTime > CURRENT_TIME))
                AND slots.appointment IS NULL
            """)
    List<AppointmentSlotEntity> findAvailableDatesByDoctorAndMedicalCenterAndSpecialty(@Param("medicalCenter") MedicalCenterEntity medicalCenter,
                                                                                       @Param("specialty") SpecialtyEntity specialty,
                                                                                       @Param("doctor") MedicalProfileEntity doctor);

    @Query("""
            SELECT CASE WHEN COUNT(slots) > 0 THEN TRUE ELSE FALSE END
            FROM AppointmentSlotEntity slots
            WHERE slots.room = :room
                AND slots.date = :date
                AND ((:startTime < slots.endTime AND :endTime > slots.startTime))
            """)
    boolean existsRoomOverlappingSlot(@Param("room") RoomEntity room,
                                      @Param("date") LocalDate date,
                                      @Param("startTime") LocalTime startTime,
                                      @Param("endTime") LocalTime endTime);

    @Query("""
            SELECT CASE WHEN COUNT(slots) > 0 THEN TRUE ELSE FALSE END
            FROM AppointmentSlotEntity slots
            WHERE slots.doctor = :doctor
                AND slots.date = :date
                AND ((:startTime < slots.endTime AND :endTime > slots.startTime))
            """)
    boolean existsDoctorOverlappingSlot(@Param("doctor") MedicalProfileEntity doctor,
                                        @Param("date") LocalDate date,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("endTime") LocalTime endTime);
}
