package com.kikisito.salus.api.constants;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAgeConstraint, LocalDate> {
    private int minimumAge;

    @Override
    public void initialize(MinimumAgeConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.minimumAge = constraintAnnotation.minimumAge();
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        if (date == null) {
            return false;
        }

        LocalDate minValidDate = LocalDate.now().minusYears(minimumAge);
        return date.isBefore(minValidDate);
    }
}
