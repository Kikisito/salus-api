package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AgendaMedicoDTO {
    private Integer id;
    private PerfilMedicoDTO medico;
    private EspecialidadDTO especialidad;
    private ConsultaDTO consulta;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionCita;
}
