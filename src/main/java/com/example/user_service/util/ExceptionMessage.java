package com.example.user_service.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessage {
    USER_NOT_FOUND_BY_ID("Пользователя с таким ID не существует"),
    CARD_NOT_FOUND_BY_ID("Карты с таким ID не существует"),
    CARD_LIMIT("Нельзя создать более 5 карт");

    private final String description;
}
