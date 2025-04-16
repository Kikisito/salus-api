package com.kikisito.salus.api.entity;

import com.kikisito.salus.api.type.CitaStatusType;
import com.kikisito.salus.api.type.CitaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "appointments")
public class AppointmentEntity extends DatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private AppointmentSlotEntity slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private UserEntity patient;

    @Column(nullable = false)
    @Builder.Default
    private CitaType type = CitaType.PRESENCIAL;

    @Column(nullable = false)
    @Builder.Default
    private CitaStatusType status = CitaStatusType.PENDIENTE;

    @Column(nullable = false)
    private String reason;
}
