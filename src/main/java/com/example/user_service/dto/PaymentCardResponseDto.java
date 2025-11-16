package com.example.user_service.dto;

import com.example.user_service.util.validation.annotation.CheckCardNumber;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCardResponseDto(

    @NotBlank
    UUID id,
    @NotBlank
    UUID userId,
    @NotBlank
    @CheckCardNumber
    Long number,
    @NotBlank
    String holder,
    @NotBlank
    LocalDateTime expirationDate,
    @NotBlank
    String active
) {}
