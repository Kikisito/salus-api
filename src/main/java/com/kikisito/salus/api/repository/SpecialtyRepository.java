package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.SpecialtyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<SpecialtyEntity, Integer> {
    Page<SpecialtyEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
