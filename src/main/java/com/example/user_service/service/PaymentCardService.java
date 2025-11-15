package com.example.user_service.service;

import com.example.user_service.entity.PaymentCard;
import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.PaymentCardType;
import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UsersRepository;
import com.example.user_service.util.specification.PaymentCardSpecification;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final UsersRepository usersRepository;

    // Create Card
    public PaymentCard createCard(UUID userId, PaymentCard card) {

        Users user = usersRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        long count = paymentCardRepository.count(
            PaymentCardSpecification.byUserId(userId));

        if (count >= 5) {
            throw new IllegalStateException("User may have no more than 5 cards.");
        }

        card.setId(null); // генерируется в БД
        card.setUser(user);
        return paymentCardRepository.save(card);
    }

    // Get Card by ID
    public PaymentCard getCard(UUID id) {
        return paymentCardRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Card not found: " + id));
    }

    // Get all cards with pagination
    public Page<PaymentCard> getAllCards(UUID userId, Pageable pageable) {
        Specification<PaymentCard> spec = PaymentCardSpecification.byUserId(userId);

        return paymentCardRepository.findAll(spec, pageable);
    }

    // Get all cards by user id
    public List<PaymentCard> getCardsByUserId(UUID userId) {
        return paymentCardRepository.findAll(
            PaymentCardSpecification.byUserId(userId)
        );
    }

    // Update card
    public PaymentCard updateCard(UUID id, PaymentCard updated) {
        PaymentCard card = getCard(id);

        card.setHolder(updated.getHolder());
        card.setActive(updated.getActive());
        card.setExpirationDate(updated.getExpirationDate());

        return paymentCardRepository.save(card);
    }

    // Activate/Deactivate card
    public PaymentCard changeStatus(UUID id, PaymentCardType status) {
        PaymentCard card = getCard(id);
        card.setActive(status);
        return paymentCardRepository.save(card);
    }
}
