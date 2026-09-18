package com.puntomartinez.millete.users.infrastructure.in.controller.validation;

import com.puntomartinez.millete.users.domain.validation.EmailValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidEmailValidator
        implements ConstraintValidator<ValidEmail, String> {

    @Override
    public boolean isValid(
            String email,
            ConstraintValidatorContext context
    ) {
        return email == null || EmailValidator.isValid(email);
    }
}