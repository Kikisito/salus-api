package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReducedUserDTO {
    private Integer id;
    private String nombre;
    private String apellidos;
    private String sexo;
}
