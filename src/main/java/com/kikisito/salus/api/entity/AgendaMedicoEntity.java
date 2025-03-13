package com.kikisito.salus.api.entity;

import com.kikisito.salus.api.type.DiaSemana;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "agenda_medico", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"medico_id", "diaSemana", "horaInicio", "horaFin"})
})
public class AgendaMedicoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private UserEntity medico;

    @Column(nullable = false)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    @Builder.Default
    private Integer duracionCita = 15; // minutos
}
