package com.example.user_service.dto;

import com.example.user_service.entity.enums.PaymentCardType;
import com.example.user_service.util.validation.annotation.CheckCardNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentCardResponseDto{
    @NotNull
    UUID id;
    @NotNull
    UUID userId;
    @NotBlank
    @CheckCardNumber
    String number;
    @NotBlank
    String holder;
    @NotNull
    LocalDate expirationDate;
    @NotNull
    PaymentCardType active;
}
