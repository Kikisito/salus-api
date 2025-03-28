package com.kikisito.salus.api.dto.response;

import com.kikisito.salus.api.dto.PerfilMedicoDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorsListResponse {
    private final long count;
    private final List<PerfilMedicoDTO> doctors;
}
