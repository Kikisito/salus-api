package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@SuperBuilder
@Table(name = "perfiles_medicos")
public class PerfilMedicoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String numeroColegiado;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "medicos_especialidades",
            joinColumns = @JoinColumn(name = "medico_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id"))
    private List<EspecialidadEntity> especialidades;

    @OneToMany(mappedBy = "perfilMedico")
    private List<CitaSlotEntity> slotsCitas;
}