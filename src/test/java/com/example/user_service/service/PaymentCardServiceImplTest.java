package com.example.user_service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.user_service.dto.ChangePaymentCardTypeRequest;
import com.example.user_service.dto.PaymentCardCreateRequest;
import com.example.user_service.dto.PaymentCardResponseDto;
import com.example.user_service.dto.PaymentCardUpdateRequest;
import com.example.user_service.entity.PaymentCard;
import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.PaymentCardType;
import com.example.user_service.entity.enums.UserType;
import com.example.user_service.exception.ConflictException;
import com.example.user_service.exception.NotFoundException;
import com.example.user_service.mapper.PaymentCardMapper;
import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UsersRepository;
import com.example.user_service.service.impl.PaymentCardServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private static UUID EXISTING_USER_ID;
    private static UUID NON_EXISTING_USER_ID;
    private static UUID EXISTING_CARD_ID;
    private static UUID NON_EXISTING_CARD_ID;

    private Users existingUser;
    private PaymentCard existingCard;
    private PaymentCardResponseDto existingCardDto;

    @BeforeAll
    static void initIds() {
        EXISTING_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        NON_EXISTING_USER_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
        EXISTING_CARD_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
        NON_EXISTING_CARD_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    }

    @BeforeEach
    void setUp() {
        existingUser = Users.builder()
            .id(EXISTING_USER_ID)
            .name("Иван")
            .surname("Иванов")
            .email("ivan@example.com")
            .active(UserType.ACTIVE)
            .build();

        existingCard = PaymentCard.builder()
            .id(EXISTING_CARD_ID)
            .user(existingUser)
            .number(String.valueOf(4000000000001234L))
            .holder("ИВАН ИВАНОВ")
            .expirationDate(LocalDate.now().plusYears(3))
            .active(PaymentCardType.ACTIVE)
            .build();

        existingCardDto = PaymentCardResponseDto.builder()
            .id(EXISTING_CARD_ID)
            .userId(EXISTING_USER_ID)
            .number(String.valueOf(4000000000001234L))
            .holder("ИВАН ИВАНОВ")
            .expirationDate(LocalDate.now().plusYears(3))
            .active(PaymentCardType.ACTIVE)
            .build();
    }

    // ===================== CREATE CARD =====================
    @Test
    void createCard_WhenUserExistsAndLimitNotReached_CreatesCard() {
        when(usersRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(existingUser));
        when(paymentCardRepository.count(any(Specification.class))).thenReturn(3L);

        PaymentCard newCard = PaymentCard.builder()
            .id(EXISTING_CARD_ID)
            .user(existingUser)
            .number(null) // будет сгенерирован
            .active(PaymentCardType.ACTIVE)
            .build();

        PaymentCard savedCard = existingCard;

        when(paymentCardMapper.toEntity(any(PaymentCardCreateRequest.class), eq(existingUser)))
            .thenReturn(newCard);
        when(paymentCardRepository.save(newCard)).thenAnswer(inv -> {
            newCard.setNumber(String.valueOf(4000000012345678L));
            newCard.setExpirationDate(LocalDate.now().plusYears(3));
            return newCard;
        });
        when(paymentCardMapper.toDto(newCard)).thenReturn(existingCardDto.toBuilder()
            .number(String.valueOf(4000000012345678L))
            .build());

        PaymentCardResponseDto result = paymentCardService.createCard(EXISTING_USER_ID);

        assertNotNull(result.getNumber());
        assertTrue(Long.parseLong(result.getNumber()) >= 4000000000000000L);
        assertEquals(PaymentCardType.ACTIVE, result.getActive());
        verify(paymentCardRepository).save(newCard);
    }

    @Test
    void createCard_WhenUserNotFound_ThrowsNotFoundException() {
        when(usersRepository.findById(NON_EXISTING_USER_ID)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> paymentCardService.createCard(NON_EXISTING_USER_ID));

        assertTrue(ex.getMessage().contains("не существует"));
    }

    @Test
    void createCard_WhenLimitReached_ThrowsConflictException() {
        when(usersRepository.findById(EXISTING_USER_ID)).thenReturn(Optional.of(existingUser));
        when(paymentCardRepository.count(any(Specification.class))).thenReturn(5L);

        ConflictException ex = assertThrows(ConflictException.class,
            () -> paymentCardService.createCard(EXISTING_USER_ID));

        assertTrue(ex.getMessage().contains("более 5 карт"));
    }

    // ===================== GET CARD BY ID =====================
    @Test
    void getCard_WhenExists_ReturnsDto() {
        when(paymentCardRepository.findById(EXISTING_CARD_ID)).thenReturn(Optional.of(existingCard));
        when(paymentCardMapper.toDto(existingCard)).thenReturn(existingCardDto);

        PaymentCardResponseDto result = paymentCardService.getCard(EXISTING_CARD_ID);

        assertEquals(existingCardDto, result);
    }

    @Test
    void getCard_WhenNotExists_ThrowsNotFoundException() {
        when(paymentCardRepository.findById(NON_EXISTING_CARD_ID)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> paymentCardService.getCard(NON_EXISTING_CARD_ID));

        assertTrue(ex.getMessage().contains("не существует"));
    }

    // ===================== GET ALL CARDS (with number filter) =====================
    @Test
    void getAllCards_WithNumberFilter_ReturnsFilteredPage() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(existingCard), pageable, 1);

        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(paymentCardMapper.toDto(existingCard)).thenReturn(existingCardDto);

        Page<PaymentCardResponseDto> result = paymentCardService.getAllCards(String.valueOf(1234L), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(existingCardDto, result.getContent().get(0));
    }

    // ===================== GET CARDS BY USER ID =====================
    @Test
    void getCardsByUserId_ReturnsUserCards() {
        when(paymentCardRepository.findAll(any(Specification.class)))
            .thenReturn(List.of(existingCard));
        when(paymentCardMapper.toDtoList(List.of(existingCard)))
            .thenReturn(List.of(existingCardDto));

        List<PaymentCardResponseDto> result = paymentCardService.getCardsByUserId(EXISTING_USER_ID);

        assertEquals(1, result.size());
        assertEquals(existingCardDto, result.get(0));
    }

    // ===================== UPDATE CARD =====================
    @Test
    void updateCard_WhenExists_UpdatesAndReturnsDto() {
        var request = PaymentCardUpdateRequest.builder()
            .holder("ПЁТР ПЕТРОВ")
            .build();

        var updatedDto = existingCardDto.toBuilder()
            .holder("ПЁТР ПЕТРОВ")
            .build();

        when(paymentCardRepository.findById(EXISTING_CARD_ID)).thenReturn(Optional.of(existingCard));
        doNothing().when(paymentCardMapper).updateEntity(existingCard, request);
        when(paymentCardRepository.save(existingCard)).thenReturn(existingCard);
        when(paymentCardMapper.toDto(existingCard)).thenReturn(updatedDto);

        PaymentCardResponseDto result = paymentCardService.updateCard(EXISTING_CARD_ID, request);

        assertEquals("ПЁТР ПЕТРОВ", result.getHolder());
        verify(paymentCardMapper).updateEntity(existingCard, request);
    }

    @Test
    void updateCard_WhenNotExists_ThrowsNotFoundException() {
        when(paymentCardRepository.findById(NON_EXISTING_CARD_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> paymentCardService.updateCard(NON_EXISTING_CARD_ID, PaymentCardUpdateRequest.builder().build()));
    }

    // ===================== CHANGE STATUS =====================
    @Test
    void changeStatus_WhenExists_ChangesStatus() {
        var frozenDto = existingCardDto.toBuilder().active(PaymentCardType.FROZEN).build();
        ChangePaymentCardTypeRequest status = ChangePaymentCardTypeRequest.builder().status(PaymentCardType.FROZEN).build();

        when(paymentCardRepository.findById(EXISTING_CARD_ID)).thenReturn(Optional.of(existingCard));
        when(paymentCardRepository.save(existingCard)).thenReturn(existingCard);
        when(paymentCardMapper.toDto(existingCard)).thenReturn(frozenDto);

        PaymentCardResponseDto result = paymentCardService.changeStatus(EXISTING_CARD_ID, status);

        assertEquals(PaymentCardType.FROZEN, existingCard.getActive());
        assertEquals(PaymentCardType.FROZEN, result.getActive());
    }

    @Test
    void changeStatus_WhenNotExists_ThrowsNotFoundException() {
        ChangePaymentCardTypeRequest status = ChangePaymentCardTypeRequest.builder().status(PaymentCardType.FROZEN).build();
        when(paymentCardRepository.findById(NON_EXISTING_CARD_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> paymentCardService.changeStatus(NON_EXISTING_CARD_ID, status));
    }

    // ===================== DELETE CARD =====================
    @Test
    void deleteCard_WhenExists_DeletesCard() {
        when(paymentCardRepository.findById(EXISTING_CARD_ID)).thenReturn(Optional.of(existingCard));

        paymentCardService.deleteCard(EXISTING_CARD_ID);

        verify(paymentCardRepository).delete(existingCard);
    }

    @Test
    void deleteCard_WhenNotExists_ThrowsNotFoundException() {
        when(paymentCardRepository.findById(NON_EXISTING_CARD_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> paymentCardService.deleteCard(NON_EXISTING_CARD_ID));
    }
}
