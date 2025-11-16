package com.example.user_service.dto;

import java.time.LocalDateTime;

public record UsersUpdateRequest(
    String name,
    String surname,
    LocalDateTime birthDate,
    String email,
    String active
) {}
