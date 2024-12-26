package com.kikisito.salus.api.entity;

import com.kikisito.salus.api.type.CitaStatusType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "consultas")
public class ConsultaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_medico_id")
    private CentroMedicoEntity centroMedico;

    @Column(nullable = false)
    private String nombre;

}
