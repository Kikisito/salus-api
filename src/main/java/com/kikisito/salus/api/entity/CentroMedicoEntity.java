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
@Table(name = "centros_medicos")
public class CentroMedicoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String lineaDireccion1;

    @Column
    private String lineaDireccion2;

    @Column(nullable = false)
    private String codigoPostal;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String municipio;

    @Column(nullable = false)
    private String localidad;

    @OneToMany(mappedBy = "centroMedico")
    private List<ConsultaEntity> consultas;
}
