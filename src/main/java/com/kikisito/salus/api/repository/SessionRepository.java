package com.kikisito.salus.api.repository;

import com.kikisito.salus.api.entity.SessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Integer> {

    Optional<SessionEntity> findByAccessToken(String token);

    Optional<SessionEntity> findByAccessTokenAndRefreshTokenAndPublicId(String accessToken, String refreshToken, String publicId);

    @Modifying
    void deleteByPublicId(String publicId);

}