package com.example.user_service.service.impl;

import static com.example.user_service.util.ExceptionMessage.CARD_LIMIT;
import static com.example.user_service.util.ExceptionMessage.CARD_NOT_FOUND_BY_ID;

import com.example.user_service.dto.PaymentCardCreateRequest;
import com.example.user_service.dto.PaymentCardResponseDto;
import com.example.user_service.dto.PaymentCardUpdateRequest;
import com.example.user_service.entity.PaymentCard;
import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.PaymentCardType;
import com.example.user_service.exception.ConflictException;
import com.example.user_service.exception.NotFoundException;
import com.example.user_service.mapper.PaymentCardMapper;
import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UsersRepository;
import com.example.user_service.service.PaymentCardService;
import com.example.user_service.util.specification.PaymentCardSpecification;
import java.time.LocalDateTime;
import java.util.List;
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
public class PaymentCardServiceImpl implements PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final UsersRepository usersRepository;
    private final PaymentCardMapper paymentCardMapper;

    // Create Card
    @Override
    public PaymentCardResponseDto createCard(UUID userId) {

        Users user = usersRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND_BY_ID.getDescription()));

        long count = paymentCardRepository.count(
            PaymentCardSpecification.byUserId(userId));

        if (count >= 5) {
            throw new ConflictException(CARD_LIMIT.getDescription());
        }
        PaymentCardCreateRequest request = new PaymentCardCreateRequest(userId);
        PaymentCard card = paymentCardMapper.toEntity(request, user);
        card.setNumber(generateCardNumber());
        card.setExpirationDate(LocalDateTime.now().plusYears(3));
        PaymentCard savedCard = paymentCardRepository.save(card);
        return paymentCardMapper.toDto(savedCard);
    }

    // Get Card by ID
    @Override
    public PaymentCardResponseDto getCard(UUID id) {
        PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND_BY_ID.getDescription()));
        return paymentCardMapper.toDto(card);
    }

    // Get all cards with pagination
    @Override
    public Page<PaymentCardResponseDto> getAllCards(Long number, Pageable pageable) {
        Specification<PaymentCard> spec = PaymentCardSpecification.numberContains(number.toString());
        Page<PaymentCard> paymentCards = paymentCardRepository.findAll(spec, pageable);

        return paymentCards.map(paymentCardMapper::toDto);
    }

    // Get all cards by user id
    @Override
    public List<PaymentCardResponseDto> getCardsByUserId(UUID userId) {
        List<PaymentCard> paymentCardList = paymentCardRepository.findAll(
            PaymentCardSpecification.byUserId(userId));
        return paymentCardMapper.toDtoList(paymentCardList);
    }

    // Update card
    @Override
    public PaymentCardResponseDto updateCard(UUID id, PaymentCardUpdateRequest updated) {
        PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND_BY_ID.getDescription()));

        paymentCardMapper.updateEntity(card, updated);
        paymentCardRepository.save(card);

        return paymentCardMapper.toDto(card);
    }

    // Activate/Deactivate card
    @Override
    public PaymentCardResponseDto changeStatus(UUID id, PaymentCardType status) {
        PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND_BY_ID.getDescription()));
        card.setActive(status);
        paymentCardRepository.save(card);
        return paymentCardMapper.toDto(card);
    }

    @Override
    public void deleteCard(UUID id) {
        PaymentCard card = paymentCardRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(CARD_NOT_FOUND_BY_ID.getDescription()));
        paymentCardRepository.delete(card);
    }

    private Long generateCardNumber() {
        return 4000_0000_0000_0000L + (long) (Math.random() * 1_0000_0000_0000_0000L);
    }
}
