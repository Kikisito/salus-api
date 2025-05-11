package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "medications")
public class MedicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column
    private String instructions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private PrescriptionEntity prescription;
}
