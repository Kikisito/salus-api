package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.RoomEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Integer> {
    Page<RoomEntity> findByNameContainingIgnoreCaseOrMedicalCenter_NameContainingIgnoreCase(String name, String medicalCenter, Pageable pageable);
}
