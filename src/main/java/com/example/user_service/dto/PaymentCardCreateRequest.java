package com.example.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record PaymentCardCreateRequest(
    @NotBlank
    UUID userId
) {}
