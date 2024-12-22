package com.kikisito.salus.api.entity;

import com.kikisito.salus.api.service.AuthService;
import com.kikisito.salus.api.type.TokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "sessions")
public class SessionEntity extends DatedEntity {

    @Id
    @GeneratedValue
    public Integer id;

    @Column(unique = true, nullable = false)
    @Builder.Default
    private String publicId = AuthService.getPublicId(); // ID pública que servirá como primera parte del token JWT

    @Column(unique = true, nullable = false)
    public String accessToken;

    @Column(unique = true, nullable = false)
    public String refreshToken;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    public TokenType tokenType = TokenType.BEARER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public UserEntity user;

}
