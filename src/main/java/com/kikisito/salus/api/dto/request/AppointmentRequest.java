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
public class AppointmentRequest {
    private Integer appointmentSlot;
    private Integer patient;
    private CitaType type; // Presencial, telefónica...
    private String reason;
}
