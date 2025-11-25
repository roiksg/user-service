package com.example.user_service.dto;

import com.example.user_service.entity.enums.PaymentCardType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePaymentCardTypeRequest {

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentCardType status;

}
