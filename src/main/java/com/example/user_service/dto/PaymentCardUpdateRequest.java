package com.example.user_service.dto;

import java.time.LocalDateTime;

public record PaymentCardUpdateRequest(
    String holder,
    LocalDateTime expirationDate,
    String active
) {}
