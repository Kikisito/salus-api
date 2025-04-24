package com.kikisito.salus.api.dto.request;

import com.kikisito.salus.api.constants.ErrorMessages;
import com.kikisito.salus.api.type.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportRequest {
    @Builder.Default
    private ReportType type = ReportType.GENERAL;

    private Integer appointment;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer doctor;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private Integer patient;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @Length(min = 1, max = 30, message = ErrorMessages.FIELD_LENGTH_INVALID)
    public String description;

    @NotNull(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    @NotBlank(message = ErrorMessages.FIELD_CANNOT_BE_BLANK)
    private String diagnosis;

    private String treatment;

    private String observations;
}
