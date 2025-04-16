package com.kikisito.salus.api.dto.request;

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
public class AppointmentSlotRequest {
    private Integer doctor;
    private Integer specialty;
    private Integer room;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
