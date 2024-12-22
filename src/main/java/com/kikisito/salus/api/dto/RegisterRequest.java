package com.kikisito.salus.api.dto;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class RegisterRequest {
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String nif;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String nombre;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String apellidos;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Email(message = ErrorMessages.INVALID_EMAIL)
    private String email;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String password;

    private String telefono;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Past(message = ErrorMessages.INVALID_DATE_PAST)
    private Date fechaNacimiento;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Valid
    private DireccionDTO direccion;
}
