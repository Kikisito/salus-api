package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.MedicalCenterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalCenterRepository extends JpaRepository<MedicalCenterEntity, Integer> {
    @Query("SELECT c FROM MedicalCenterEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<MedicalCenterEntity> findByNameContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
