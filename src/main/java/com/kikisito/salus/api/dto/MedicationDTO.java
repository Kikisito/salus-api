package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicationDTO {
    private Integer id;
    private String name;
    private String dosage;
    private BigDecimal frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;
    private Integer prescriptionId;
}
