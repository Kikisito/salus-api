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
@Table(name = "medicos")
@PrimaryKeyJoinColumn(name = "user_id")
public class MedicoEntity extends UserEntity {
    @Column(nullable = false, unique = true)
    private String numeroColegiado;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "medicos_especialidades",
            joinColumns = @JoinColumn(name = "medico_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidad_id"))
    private List<EspecialidadEntity> especialidades;

    @OneToMany(mappedBy = "medico")
    private List<CitaSlotEntity> slotsCitas;
}