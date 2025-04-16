package com.kikisito.salus.api.dto.response;

import com.kikisito.salus.api.dto.SpecialtyDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpecialtiesListResponse {
    private final long count;
    private final List<SpecialtyDTO> specialties;
}
