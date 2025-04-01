package com.kikisito.salus.api.dto.response;

import com.kikisito.salus.api.dto.CentroMedicoDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MedicalCentersListResponse {
    private final long count;
    private final List<CentroMedicoDTO> medicalCenters;
}
