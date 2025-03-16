package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CitaSlotDTO {
    private Integer id;
    private PerfilMedicoDTO perfilMedico;
    private EspecialidadDTO especialidad;
    private ConsultaDTO consulta;
    private LocalDateTime fecha;
    private CitaEntity cita;
}
