package com.kikisito.salus.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DireccionDTO {
    //@NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String lineaDireccion1;

    private String lineaDireccion2;

    //@NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String codigoPostal;

    //@NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String pais;

    //@NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String provincia;

    //@NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String municipio;

    //@NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String localidad;
}
