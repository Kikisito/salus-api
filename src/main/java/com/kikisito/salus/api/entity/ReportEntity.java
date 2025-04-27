package com.kikisito.salus.api.entity;

import com.kikisito.salus.api.type.ReportType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "reports")
public class ReportEntity extends DatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportType type = ReportType.GENERAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private AppointmentEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private SpecialtyEntity specialty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private MedicalProfileEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private UserEntity patient;

    @Column(nullable = false, length = 30)
    private String description;

    @Column(nullable = false)
    private String diagnosis;

    @Column
    private String treatment;

    @Column
    private String observations;
}
