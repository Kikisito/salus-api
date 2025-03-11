package com.kikisito.salus.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kikisito.salus.api.type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
