package com.example.user_service.util.validation;

import com.example.user_service.util.validation.annotation.CheckCardNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CardNumberValidator implements ConstraintValidator<CheckCardNumber, String> {

    @Override
    public boolean isValid(String number, ConstraintValidatorContext context) {
        String cardNumber=String.valueOf(number);
        return cardNumber.matches(RegExp.CARD_NUMBER_REG);
    }

}
