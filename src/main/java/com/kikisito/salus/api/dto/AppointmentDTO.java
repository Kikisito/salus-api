package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.type.AppointmentStatusType;
import com.kikisito.salus.api.type.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppointmentDTO {
    private Integer id;
    private AppointmentSlotDTO slot;
    private UserDTO patient;
    private AppointmentType type;
    private AppointmentStatusType status;
    private String reason;
    private String doctorObservations;
    private List<ReportDTO> reports;
}
