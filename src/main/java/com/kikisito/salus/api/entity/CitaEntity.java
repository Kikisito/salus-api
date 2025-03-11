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
@Table(name = "citas")
public class CitaEntity extends DatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private CitaSlotEntity slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private UserEntity paciente;

    @Column(nullable = false)
    @Builder.Default
    private CitaType tipo = CitaType.PRESENCIAL;

    @Column(nullable = false)
    @Builder.Default
    private CitaStatusType estado = CitaStatusType.PENDIENTE;

    @Column(nullable = false)
    private String motivo;
}
