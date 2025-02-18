package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UsuarioDTO {
    private Integer id;
    private String nombre;
    private String apellidos;
    private String nif;
    private String email;
    private String telefono;
    private Date fechaNacimiento;
    private DireccionDTO direccion;
}
