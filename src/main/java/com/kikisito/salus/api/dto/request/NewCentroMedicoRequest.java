package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.dto.ConsultaDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NewCentroMedicoRequest {
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String nombre;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String email;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String telefono;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String lineaDireccion1;

    private String lineaDireccion2;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String codigoPostal;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String pais;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String provincia;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String municipio;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String localidad;
}
