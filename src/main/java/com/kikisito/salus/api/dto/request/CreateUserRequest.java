package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.constants.MinimumAgeConstraint;
import com.kikisito.salus.api.constants.NifConstraint;
import com.kikisito.salus.api.constants.PasswordConstraint;
import com.kikisito.salus.api.dto.DireccionDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@Builder
public class CreateUserRequest {
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NifConstraint(message = ErrorMessages.INVALID_NIF)
    private String nif;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Length(min = 2, max = 50, message = ErrorMessages.FIELD_LENGTH_INVALID)
    private String nombre;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Length(min = 2, max = 50, message = ErrorMessages.FIELD_LENGTH_INVALID)
    private String apellidos;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Email(message = ErrorMessages.INVALID_EMAIL)
    @Length(max = 100, message = ErrorMessages.FIELD_LENGTH_INVALID)
    private String email;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Length(max = 20, message = ErrorMessages.FIELD_LENGTH_INVALID)
    @Pattern(regexp = "^(\\+?[1-9]\\d{1,14})|(\\d{1,14})$", message = ErrorMessages.FIELD_IS_NOT_VALID)
    private String telefono;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Past(message = ErrorMessages.INVALID_DATE_PAST)
    @MinimumAgeConstraint(minimumAge = 18, message = ErrorMessages.INVALID_DATE_18_YEARS_OLD)
    private LocalDate fechaNacimiento;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String sexo;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Valid
    private DireccionDTO direccion;
}
