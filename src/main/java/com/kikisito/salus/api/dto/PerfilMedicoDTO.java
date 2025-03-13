package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PerfilMedicoDTO {
    private UsuarioDTO user;
    private String numeroColegiado;
    private List<EspecialidadDTO> especialidades;
}
