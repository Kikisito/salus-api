package com.kikisito.salus.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DireccionDTO {
    private Integer id;

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
