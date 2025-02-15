package com.kikisito.salus.api.constants;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Calendar;
import java.util.Date;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAgeConstraint, Date> {
    private int minimumAge;

    @Override
    public void initialize(MinimumAgeConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.minimumAge = constraintAnnotation.minimumAge();
    }

    @Override
    public boolean isValid(Date date, ConstraintValidatorContext constraintValidatorContext) {
        if (date == null) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -minimumAge);
        Date minValidDate = calendar.getTime();

        return date.before(minValidDate);
    }
}
