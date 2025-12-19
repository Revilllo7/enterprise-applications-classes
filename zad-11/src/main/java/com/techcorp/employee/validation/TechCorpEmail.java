package com.techcorp.employee.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target({ FIELD, METHOD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = TechCorpEmailValidator.class)
public @interface TechCorpEmail {
	String message() default "Email musi należeć do domeny techcorp.com";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
