package com.kikisito.salus.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Getter
@SuperBuilder
public class DatedEntity {
    @Column(nullable = false)
    @Builder.Default
    private Date createdAt = new Date();

    @Column(nullable = false)
    @Builder.Default
    @Setter
    private Date updatedAt = new Date();
}
