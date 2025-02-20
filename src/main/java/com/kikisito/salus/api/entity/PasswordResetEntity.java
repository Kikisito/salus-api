package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String token;

    @OneToOne(targetEntity = UserEntity.class)
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private UserEntity userEntity;
}
