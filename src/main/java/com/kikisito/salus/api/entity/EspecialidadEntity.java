package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "especialidades")
public class EspecialidadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column
    private String descripcion;

    @ManyToMany(mappedBy = "especialidades")
    private List<PerfilMedicoEntity> perfilesMedicos;

    @OneToMany(mappedBy = "especialidad")
    private List<CitaSlotEntity> slotsCitas;
}