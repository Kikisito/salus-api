package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.PerfilMedicoEntity;
import com.kikisito.salus.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilMedicoRepository extends JpaRepository<PerfilMedicoEntity, Integer> {
    boolean existsMedicoEntitiesByUser(UserEntity user);

    boolean existsMedicoEntitiesByNumeroColegiado(String numeroColegiado);
}
