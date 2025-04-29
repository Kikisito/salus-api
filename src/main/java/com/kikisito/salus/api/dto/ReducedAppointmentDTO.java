package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.type.AppointmentStatusType;
import com.kikisito.salus.api.type.AppointmentType;
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
    private AppointmentType type;
    private AppointmentStatusType status;
    private String reason;
    // Las observaciones del doctor no se envían al paciente, son como notas privadas de los profesionales
    //private String doctorObservations;
}
