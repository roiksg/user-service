package com.example.user_service.dto;

import com.example.user_service.util.validation.RegExp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;

public record UsersCreateRequest(
    @NotBlank
    UUID id,
    @NotBlank
    String name,
    @NotBlank
    String surname,
    @NotBlank
    LocalDateTime birthDate,
    @NotBlank (message = "email не иожет быть пустым")
    @Pattern(message = "Некорректный email", regexp = RegExp.EMAIL_REG)
    String email
) {}
