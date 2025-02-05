package com.kikisito.salus.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UsuarioDTO {
    private String nombre;
    private String apellidos;
    private String nif;
    private String email;
    private String telefono;
    private Date fechaNacimiento;
    private DireccionDTO direccion;
}
