package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.MedicalCenterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalCenterRepository extends JpaRepository<MedicalCenterEntity, Integer> {
    @Query("SELECT c FROM MedicalCenterEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<MedicalCenterEntity> findByNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM MedicalCenterEntity c ORDER BY c.id ASC LIMIT 1")
    Optional<MedicalCenterEntity> findFirst();

    @Query("SELECT DISTINCT mc FROM MedicalCenterEntity mc " +
            "JOIN RoomEntity r ON r.medicalCenter.id = mc.id " +
            "JOIN AppointmentSlotEntity slots ON slots.room.id = r.id " +
            "WHERE slots.specialty.id = :specialtyId " +
                "AND (slots.date > CURRENT_DATE OR (slots.date = CURRENT_DATE AND slots.startTime > CURRENT_TIME)) " +
                "AND slots.appointment IS NULL")
    Page<MedicalCenterEntity> findByAvailableSpecialty(@Param("specialtyId") Integer specialtyId, Pageable pageable);

    @Query("SELECT DISTINCT mc FROM MedicalCenterEntity mc " +
            "JOIN RoomEntity r ON r.medicalCenter.id = mc.id " +
            "JOIN AppointmentSlotEntity slots ON slots.room.id = r.id " +
            "WHERE slots.specialty.id = :specialtyId " +
                "AND (slots.date > CURRENT_DATE OR (slots.date = CURRENT_DATE AND slots.startTime > CURRENT_TIME)) " +
                "AND slots.appointment IS NULL " +
                "AND (LOWER(mc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(mc.addressLine1) LIKE LOWER(CONCAT('%', :search, '%'))" +
                    "OR (LOWER(mc.locality) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(mc.municipality) LIKE LOWER(CONCAT('%', :search, '%')))" +
                ")")
    Page<MedicalCenterEntity> searchByAvailableSpecialty(@Param("specialtyId") Integer specialtyId, @Param("search") String search, Pageable pageable);
}
