package com.kikisito.salus.api.constants;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * This annotation is used to validate the NIF format.
 *
 * @see NifValidator
 */
@Documented
@Constraint(validatedBy = MinimumAgeValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumAgeConstraint {
    String message() default "Invalid date. You must be at least 18 years old";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int minimumAge() default 18;
}
