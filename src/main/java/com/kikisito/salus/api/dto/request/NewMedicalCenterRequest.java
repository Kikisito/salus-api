package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NewMedicalCenterRequest {
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String name;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String email;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String phone;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String zipCode;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String country;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String province;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String municipality;

    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String locality;
}
