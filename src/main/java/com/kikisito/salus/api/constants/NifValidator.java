package com.kikisito.salus.api.constants;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NifValidator implements ConstraintValidator<NifConstraint, String> {
    @Override
    public void initialize(NifConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String nif, ConstraintValidatorContext constraintValidatorContext) {
        if(nif == null) {
            return false;
        }

        // Comprobación de DNI/NIE
        return nif.matches("^\\d{8}[A-Z]$") || nif.matches("^[XYZ]\\d{7}[A-Z]$");
    }
}
