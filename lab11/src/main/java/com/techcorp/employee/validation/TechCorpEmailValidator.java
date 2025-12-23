package com.techcorp.employee.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TechCorpEmailValidator implements ConstraintValidator<TechCorpEmail, String> {
	private static final String REQUIRED_DOMAIN = "@techcorp.com";

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) return false;
		String email = value.trim().toLowerCase();
		return !email.isEmpty() && email.endsWith(REQUIRED_DOMAIN);
	}
}
