package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.type.CitaStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppointmentDTO {
    private Integer id;
    private AppointmentSlotDTO slot;
    private UserDTO patient;
    private CitaStatusType type;
    private CitaStatusType status;
    private String reason;
}
