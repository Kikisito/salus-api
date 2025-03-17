package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.type.CitaStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CitaDTO {
    private Integer id;
    private CitaSlotDTO slot;
    private UsuarioDTO paciente;
    private CitaStatusType tipo;
    private CitaStatusType estado;
    private String motivo;
}
