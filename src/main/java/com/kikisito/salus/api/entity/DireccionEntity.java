package com.kikisito.salus.api.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Builder
@Table(name = "direcciones")
public class DireccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

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

    @OneToOne(mappedBy = "direccion")
    private UserEntity user;

}