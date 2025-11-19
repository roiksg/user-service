package com.example.user_service.controller;

import com.example.user_service.IntegrationTest;
import com.example.user_service.SharedPostgresContainer;
import com.example.user_service.dto.PaymentCardResponseDto;
import com.example.user_service.dto.PaymentCardUpdateRequest;
import com.example.user_service.entity.enums.PaymentCardType;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(
    scripts = "/data.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@TestPropertySource(properties = {
    "spring.cache.type=NONE"
})
@ContextConfiguration(initializers = SharedPostgresContainer.Initializer.class)
public class PaymentCardControllerIT extends IntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    // Пользователи из data.sql
    private static final UUID USER_0_CARDS = UUID.fromString("11111111-1111-1111-1111-111111111111"); // Иван Иванов
    private static final UUID USER_1_CARD  = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_3_CARDS = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID USER_5_CARDS = UUID.fromString("66666666-6666-6666-6666-666666666666"); // уже на лимите

    private static final UUID NON_EXISTENT_USER = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID NON_EXISTENT_CARD = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");


    @Test
    void createCard_WhenUserHasLessThan5_ShouldCreateAndReturn201() {
        webTestClient.post()
            .uri("/auth/payment-card/create-card")
            .header("userId", USER_0_CARDS.toString())
            .exchange()
            .expectStatus().isCreated()
            .expectBody(PaymentCardResponseDto.class)
            .value(dto -> {
                assertThat(dto.getUserId()).isEqualTo(USER_0_CARDS);
                assertThat(dto.getNumber())
                    .hasSize(16)
                    .startsWith("4")
                    .containsOnlyDigits();
                assertThat(dto.getExpirationDate()).isAfterOrEqualTo(LocalDate.now().plusYears(3));
                assertThat(dto.getActive()).isEqualTo(PaymentCardType.ACTIVE);
            });
    }

    @Test
    void createCard_WhenUserAlreadyHas5_ShouldReturn409() {
        webTestClient.post()
            .uri("/auth/payment-card/create-card")
            .header("userId", USER_5_CARDS.toString())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT); // CARD_LIMIT
    }

    @Test
    void createCard_WhenUserNotFound_ShouldReturn404() {
        webTestClient.post()
            .uri("/auth/payment-card/create-card")
            .header("userId", NON_EXISTENT_USER.toString())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void getCard_WhenExists_ShouldReturnCard() {
        UUID existingCardId = UUID.fromString("a0000000-0000-0000-0000-000000000001");

        webTestClient.get()
            .uri("/auth/payment-card/get-card")
            .header("id", existingCardId.toString())
            .exchange()
            .expectStatus().isOk()
            .expectBody(PaymentCardResponseDto.class)
            .value(dto -> {
                assertThat(dto.getId()).isEqualTo(existingCardId);
                assertThat(dto.getUserId()).isEqualTo(USER_1_CARD);
                assertThat(dto.getActive()).isEqualTo(PaymentCardType.ACTIVE);
            });
    }

    @Test
    void getCard_WhenNotExists_ShouldReturn404() {
        webTestClient.get()
            .uri("/auth/payment-card/get-card")
            .header("id", NON_EXISTENT_CARD.toString())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void getAllCards_NoFilter_ShouldReturnPage() {
        webTestClient.get()
            .uri("/auth/payment-card/list-card")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(15) // всего 1+2+3+4+5 = 15 карт в data.sql
            .jsonPath("$.totalElements").isEqualTo(15)
            .jsonPath("$.size").isEqualTo(20);
    }

    @Test
    void getAllCards_WithNumberFilter_ShouldReturnOnlyMatching() {
        // в data.sql номера не заполнены, но после создания они генерируются начиная с 4000...
        // поэтому сначала создадим одну карту, чтобы точно знать номер
        PaymentCardResponseDto created = webTestClient.post()
            .uri("/auth/payment-card/create-card")
            .header("userId", USER_0_CARDS.toString())
            .exchange()
            .expectStatus().isCreated()
            .expectBody(PaymentCardResponseDto.class)
            .returnResult()
            .getResponseBody();

        String number = created.getNumber();

        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/auth/payment-card/list-card")
                .queryParam("number", number)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.content[0].number").isEqualTo(number);
    }

    @Test
    void getCardsByUserId_WhenUserHasCards_ShouldReturnList() {
        webTestClient.post()
            .uri("/auth/payment-card/get-list-user-card")
            .header("userId", USER_3_CARDS.toString())
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(PaymentCardResponseDto.class)
            .value(list -> {
                assertThat(list).hasSize(3);
                assertThat(list).allMatch(card -> card.getUserId().equals(USER_3_CARDS));
                assertThat(list).filteredOn(card -> card.getActive() == PaymentCardType.ACTIVE)
                    .hasSize(2);
            });
    }

    @Test
    void getCardsByUserId_WhenUserHasNoCards_ShouldReturnEmptyList() {
        webTestClient.post()
            .uri("/auth/payment-card/get-list-user-card")
            .header("userId", USER_0_CARDS.toString())
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(PaymentCardResponseDto.class)
            .hasSize(0);
    }

    @Test
    void updateCard_ShouldUpdateFieldsAndReturnUpdatedDto() {
        UUID cardId = UUID.fromString("a0000000-0000-0000-0000-000000000004"); // ACTIVE карта у Марии

        var updateRequest = PaymentCardUpdateRequest.builder()
            .holder("Новое Имя Держателя")
            .expirationDate(LocalDate.now().plusYears(5))
            .active(PaymentCardType.FROZEN)
            .build();

        webTestClient.post()
            .uri("/auth/payment-card/update-card")
            .header("id", cardId.toString())
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(PaymentCardResponseDto.class)
            .value(dto -> {
                assertThat(dto.getId()).isEqualTo(cardId);
                assertThat(dto.getHolder()).isEqualTo("Новое Имя Держателя");
                assertThat(dto.getExpirationDate()).isEqualTo(updateRequest.getExpirationDate());
                assertThat(dto.getActive()).isEqualTo(PaymentCardType.FROZEN);
            });
    }

    @Test
    void changeStatus_ShouldChangeStatusOnly() {
        UUID cardId = UUID.fromString("a0000000-0000-0000-0000-000000000007");

        webTestClient.post()
            .uri("/auth/payment-card/change-card-status")
            .header("id", cardId.toString())
            .bodyValue(PaymentCardType.FROZEN)
            .exchange()
            .expectStatus().isOk()
            .expectBody(PaymentCardResponseDto.class)
            .value(dto -> assertThat(dto.getActive()).isEqualTo(PaymentCardType.FROZEN));

        // обратно в ACTIVE
        webTestClient.post()
            .uri("/auth/payment-card/change-card-status")
            .header("id", cardId.toString())
            .bodyValue(PaymentCardType.ACTIVE)
            .exchange()
            .expectStatus().isOk()
            .expectBody(PaymentCardResponseDto.class)
            .value(dto -> assertThat(dto.getActive()).isEqualTo(PaymentCardType.ACTIVE));
    }

    @Test
    void deleteCard_WhenExists_ShouldDeleteAndReturn200() {
        // сначала создаём временную карту
        PaymentCardResponseDto tempCard = webTestClient.post()
            .uri("/auth/payment-card/create-card")
            .header("userId", USER_0_CARDS.toString())
            .exchange()
            .expectStatus().isCreated()
            .expectBody(PaymentCardResponseDto.class)
            .returnResult()
            .getResponseBody();

        UUID tempId = tempCard.getId();

        // удаляем
        webTestClient.post()
            .uri("/auth/payment-card/remove-card")
            .header("id", tempId.toString())
            .exchange()
            .expectStatus().isOk();

        // проверяем, что больше не находится
        webTestClient.get()
            .uri("/auth/payment-card/get-card")
            .header("id", tempId.toString())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void deleteCard_WhenNotExists_ShouldReturn404() {
        webTestClient.post()
            .uri("/auth/payment-card/remove-card")
            .header("id", NON_EXISTENT_CARD.toString())
            .exchange()
            .expectStatus().isNotFound();
    }
}
