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
public class UsuarioDTO {
    private Integer id;
    private String nombre;
    private String apellidos;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPasswordChange;
    private String sexo;
    private String nif;
    private String email;
    private String telefono;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;
    private DireccionDTO direccion;
    private List<RoleType> rolesList;
}
