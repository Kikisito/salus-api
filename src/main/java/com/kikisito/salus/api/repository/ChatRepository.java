package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.ChatEntity;
import com.kikisito.salus.api.entity.MedicalProfileEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Integer> {
    List<ChatEntity> findByPatientOrderByUpdatedAtDesc(UserEntity patient);
    List<ChatEntity> findByDoctorOrderByUpdatedAtDesc(MedicalProfileEntity doctor);
    Optional<ChatEntity> findByPatientAndDoctor(UserEntity patient, MedicalProfileEntity doctor);
}
