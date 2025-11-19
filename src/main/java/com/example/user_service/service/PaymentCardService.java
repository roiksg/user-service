package com.example.user_service.service;

import com.example.user_service.dto.PaymentCardResponseDto;
import com.example.user_service.dto.PaymentCardUpdateRequest;
import com.example.user_service.entity.enums.PaymentCardType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentCardService {

    PaymentCardResponseDto createCard(UUID userId);
    PaymentCardResponseDto getCard(UUID id);
    Page<PaymentCardResponseDto> getAllCards(String number, Pageable pageable);
    List<PaymentCardResponseDto> getCardsByUserId(UUID userId);
    PaymentCardResponseDto updateCard(UUID id, PaymentCardUpdateRequest updated);
    PaymentCardResponseDto changeStatus(UUID id, PaymentCardType status);
    void deleteCard(UUID id);

}
