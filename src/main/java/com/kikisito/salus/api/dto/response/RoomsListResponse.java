package com.kikisito.salus.api.dto.response;

import com.kikisito.salus.api.dto.ConsultaDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomsListResponse {
    private final long count;
    private final List<ConsultaDTO> rooms;
}
