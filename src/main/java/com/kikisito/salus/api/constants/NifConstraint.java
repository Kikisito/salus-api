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
@Constraint(validatedBy = NifValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NifConstraint {
    String message() default "Invalid NIF format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
