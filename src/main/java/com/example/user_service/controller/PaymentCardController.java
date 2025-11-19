package com.example.user_service.controller;

import com.example.user_service.dto.PaymentCardResponseDto;
import com.example.user_service.dto.PaymentCardUpdateRequest;
import com.example.user_service.entity.enums.PaymentCardType;
import com.example.user_service.service.PaymentCardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/payment-card")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping("/create-card")
    @ResponseStatus (HttpStatus.CREATED)
    public PaymentCardResponseDto createCard(@RequestHeader UUID userId) {
        return paymentCardService.createCard(userId);
    }

    @GetMapping("/get-card")
    @ResponseStatus (HttpStatus.OK)
    public PaymentCardResponseDto getCard(@RequestHeader UUID id) {
        return paymentCardService.getCard(id);
    }

    @GetMapping("/list-card")
    @ResponseStatus (HttpStatus.OK)
    public Page<PaymentCardResponseDto> getAllCards(
        @RequestParam(required = false) String number,
        @PageableDefault(size = 20, sort = "number") Pageable pageable) {
        return paymentCardService.getAllCards(number, pageable);
    }

    @PostMapping("/get-list-user-card")
    @ResponseStatus (HttpStatus.OK)
    public List<PaymentCardResponseDto> getCardsByUserId(@RequestHeader UUID userId) {
        return paymentCardService.getCardsByUserId(userId);
    }

    @PostMapping("/update-card")
    @ResponseStatus (HttpStatus.OK)
    public PaymentCardResponseDto updateCard(@RequestHeader UUID id, @RequestBody @Valid PaymentCardUpdateRequest updated) {
        return paymentCardService.updateCard(id, updated);
    }

    @PostMapping("/change-card-status")
    @ResponseStatus (HttpStatus.OK)
    public PaymentCardResponseDto changeStatus(@RequestHeader UUID id, @RequestBody PaymentCardType status) {
        return paymentCardService.changeStatus(id, status);
    }

    @PostMapping("/remove-card")
    @ResponseStatus (HttpStatus.OK)
    public void deleteCard(@RequestHeader UUID id) {
        paymentCardService.deleteCard(id);
    }
}
