package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.type.CitaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CitaRequest {
    private Integer citaSlot;
    private Integer paciente;
    private CitaType tipo; // Presencial, telefónica...
    private String motivo;
}
