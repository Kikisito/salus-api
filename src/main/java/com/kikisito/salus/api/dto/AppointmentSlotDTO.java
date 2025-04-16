package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppointmentSlotDTO {
    private Integer id;
    private MedicalProfileDTO doctor;
    private SpecialtyDTO specialty;
    private RoomDTO room;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentDTO appointment;
}
