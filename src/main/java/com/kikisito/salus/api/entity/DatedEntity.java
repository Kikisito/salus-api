package com.kikisito.salus.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Getter
@SuperBuilder
public class DatedEntity {
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    @Setter
    private LocalDateTime updatedAt = LocalDateTime.now();
}
