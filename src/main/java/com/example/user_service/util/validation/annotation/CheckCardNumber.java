package com.example.user_service.util.validation.annotation;

import com.example.user_service.util.validation.CardNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CardNumberValidator.class)
public @interface CheckCardNumber {

    String INVALID_DELIVERY_DATE = "Invalid card number";

    String message() default INVALID_DELIVERY_DATE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
