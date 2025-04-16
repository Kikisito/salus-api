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
public class SpecialtyDTO {
    private Integer id;
    private String name;
    private String description;
    //private List<MedicalProfileDTO> doctors;
    //private List<AppointmentSlotDTO> slots;
    //private List<DoctorScheduleDTO> schedules;
}
