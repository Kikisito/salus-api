package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CentroMedicoDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String lineaDireccion1;
    private String lineaDireccion2;
    private String codigoPostal;
    private String pais;
    private String provincia;
    private String municipio;
    private String localidad;
}
