package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
    @JoinColumn(name = "centro_medico_id", nullable = false)
    private CentroMedicoEntity centroMedico;

    @Column(nullable = false)
    private String nombre;

    // Cascade para borrar citas
    // antes de borrarlas se debería ver si se notifican usuarios o se hace otra acción
    @OneToMany(mappedBy = "consulta")
    private List<CitaSlotEntity> citas;

    @OneToMany(mappedBy = "consulta")
    private List<AgendaMedicoEntity> agendas;
}
