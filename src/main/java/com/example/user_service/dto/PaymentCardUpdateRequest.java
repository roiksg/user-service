package com.example.user_service.dto;

import com.example.user_service.entity.enums.PaymentCardType;
import java.time.LocalDate;
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
public class PaymentCardUpdateRequest{
    String holder;
    LocalDate expirationDate;
    PaymentCardType active;
}
