package com.example.user_service.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record UsersUpdateRequest(
    String name,
    String surname,
    LocalDateTime birthDate,
    String email,
    String active
) {}
