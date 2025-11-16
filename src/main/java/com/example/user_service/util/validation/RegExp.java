package com.example.user_service.util.validation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegExp {

    /**
     * Регулярное выражение для проверки номера карты.
     * <p>
     * Он должен состоять только из цифр и иметь длину 16 символов.
     */
    public final String CARD_NUMBER_REG = "^\\d{16}$";

    public final String EMAIL_REG = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

}
