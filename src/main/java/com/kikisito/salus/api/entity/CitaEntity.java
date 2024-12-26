package com.kikisito.salus.api.entity;

import com.kikisito.salus.api.type.CitaStatusType;
import com.kikisito.salus.api.type.CitaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private CitaType tipo = CitaType.PRESENCIAL;

    @Column(nullable = false)
    private String motivo;

    @Column(nullable = false)
    private CitaStatusType estado = CitaStatusType.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private UserEntity paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private UserEntity medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id")
    private EspecialidadEntity especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    private ConsultaEntity consulta;

}
