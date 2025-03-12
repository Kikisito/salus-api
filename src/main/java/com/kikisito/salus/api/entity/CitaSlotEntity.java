package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "citas_slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"medico_id", "diaSemana", "horaInicio", "horaFin"})
})
public class CitaSlotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private PerfilMedicoEntity perfilMedico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private EspecialidadEntity especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", nullable = false)
    private ConsultaEntity consulta;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @OneToOne(mappedBy = "slot")
    private CitaEntity cita;
}
