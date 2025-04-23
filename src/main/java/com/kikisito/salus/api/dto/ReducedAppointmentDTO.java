package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.type.CitaStatusType;
import com.kikisito.salus.api.type.CitaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReducedAppointmentDTO {
    private Integer id;
    private AppointmentSlotDTO slot;
    private UserDTO patient;
    private CitaType type;
    private CitaStatusType status;
    private String reason;
    //private String doctorObservations;
}
