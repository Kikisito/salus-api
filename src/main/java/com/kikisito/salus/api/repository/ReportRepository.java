package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.AppointmentEntity;
import com.kikisito.salus.api.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Integer> {
    List<ReportEntity> findByAppointment(AppointmentEntity appointment);
}
