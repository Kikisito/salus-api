package com.kikisito.salus.api.dto.response;

import com.kikisito.salus.api.dto.UsuarioDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UsersListResponse {
    private final long count;
    private final List<UsuarioDTO> users;
}
