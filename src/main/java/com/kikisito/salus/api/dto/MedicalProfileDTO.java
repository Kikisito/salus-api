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
public class MedicalProfileDTO {
    private Integer id;
    private UserDTO user;
    private String license;
    private List<SpecialtyDTO> specialties;
}
