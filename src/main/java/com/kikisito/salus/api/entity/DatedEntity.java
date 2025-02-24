package com.kikisito.salus.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Getter
@SuperBuilder
public class DatedEntity {
    @Column(nullable = false)
    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    @Column(nullable = false)
    @Builder.Default
    @Setter
    private LocalDate updatedAt = LocalDate.now();
}
