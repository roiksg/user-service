package com.example.user_service.controller;

import com.example.user_service.IntegrationTest;
import com.example.user_service.SharedPostgresContainer;
import com.example.user_service.dto.ChangeUserStatusRequest;
import com.example.user_service.dto.UsersCreateRequest;
import com.example.user_service.dto.UsersResponseDto;
import com.example.user_service.dto.UsersUpdateRequest;
import com.example.user_service.entity.enums.UserType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для UserController
 * Наследуемся от IntegrationTest → автоматически поднимается PostgreSQL + Testcontainers
 * @Sql подгружает data.sql перед каждым тестом
 */
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
class UserControllerIT extends IntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final UUID USER_WITH_0_CARDS = UUID.fromString("11111111-1111-1111-1111-111111111111"); // Иван Иванов
    private static final UUID USER_WITH_3_CARDS = UUID.fromString("44444444-4444-4444-4444-444444444444"); // Мария КузнецоваЕкатерина Васильева
    private static final UUID NON_EXISTENT_USER = UUID.fromString("99999999-9999-9999-9999-999999999999");


    @Test
    void createUser_ShouldCreateUserAndReturn201() {
        UUID newId = UUID.randomUUID();
        var request = UsersCreateRequest.builder()
            .id(newId)
            .name("Алексей")
            .surname("Тестовый")
            .birthDate(LocalDate.of(1995, 5, 15))
            .email("alex" + System.nanoTime() + "@test.com")
            .build();

        webTestClient.post()
            .uri("/auth/user/create-user")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.active").isEqualTo("ACTIVE")
            .jsonPath("$.id").isEqualTo(newId.toString())
            .jsonPath("$.name").isEqualTo("Алексей")
            .jsonPath("$.cards").isEmpty();
    }

    @Test
    void getUser_WhenExists_ShouldReturnUserWithCards() {
        webTestClient.post()
            .uri("/auth/user/get-user")
            .header("id", USER_WITH_3_CARDS.toString())
            .exchange()
            .expectStatus().isOk()
            .expectBody(UsersResponseDto.class)
            .value(dto -> {
                assertThat(dto.getName()).isEqualTo("Мария");
                assertThat(dto.getSurname()).isEqualTo("Кузнецова");
                assertThat(dto.getActive()).isEqualTo(UserType.FROZEN);
                assertThat(dto.getCards()).hasSize(3);
            });
    }

    @Test
    void getUser_WhenNotExists_ShouldReturn404() {
        webTestClient.post()
            .uri("/auth/user/get-user")
            .header("id", NON_EXISTENT_USER.toString())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void updateUser_ShouldUpdateFieldsAndReturnUpdatedDto() {
        var updateRequest = UsersUpdateRequest.builder()
            .name("Мария Обновлённая")
            .email("maria.updated@example.com")
            .build();

        webTestClient.post()
            .uri("/auth/user/update-user")
            .header("id", USER_WITH_3_CARDS.toString())
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UsersResponseDto.class)
            .value(dto -> {
                assertThat(dto.getName()).isEqualTo("Мария Обновлённая");
                assertThat(dto.getEmail()).isEqualTo("maria.updated@example.com");
                assertThat(dto.getCards()).hasSize(3);
            });
    }

    @Test
    void changeStatus_ToFrozen_ShouldChangeStatus() {
        ChangeUserStatusRequest status = ChangeUserStatusRequest.builder().status(UserType.FROZEN).build();

        webTestClient.post()
            .uri("/auth/user/change-user-status")
            .header("id", USER_WITH_0_CARDS.toString())
            .bodyValue(status)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UsersResponseDto.class)
            .value(dto -> assertThat(dto.getActive()).isEqualTo(UserType.FROZEN));
    }

    @Test
    void changeStatus_ToActive_ShouldChangeBack() {
        ChangeUserStatusRequest statusFrozen = ChangeUserStatusRequest.builder().status(UserType.FROZEN).build();
        ChangeUserStatusRequest statusActive = ChangeUserStatusRequest.builder().status(UserType.ACTIVE).build();

        // Сначала заморозим
        webTestClient.post()
            .uri("/auth/user/change-user-status")
            .header("id", USER_WITH_0_CARDS.toString())
            .bodyValue(statusFrozen)
            .exchange();

        // Потом разморозим
        webTestClient.post()
            .uri("/auth/user/change-user-status")
            .header("id", USER_WITH_0_CARDS.toString())
            .bodyValue(statusActive)
            .exchange()
            .expectStatus().isOk()
            .expectBody(UsersResponseDto.class)
            .value(dto -> assertThat(dto.getActive()).isEqualTo(UserType.ACTIVE));
    }

    @Test
    void removeUser_ShouldDeleteUserAndReturn200() {
        // Сначала создаём временного пользователя
        UUID tempId = UUID.randomUUID();
        webTestClient.post()
            .uri("/auth/user/create-user")
            .bodyValue(UsersCreateRequest.builder()
                .id(tempId)
                .name("ДляУдаления")
                .surname("Тест")
                .birthDate(LocalDate.now().minusYears(30))
                .email("delete" + System.nanoTime() + "@test.com")
                .build())
            .exchange()
            .expectStatus().isCreated();

        // Удаляем
        webTestClient.post()
            .uri("/auth/user/remove-user")
            .header("id", tempId.toString())
            .exchange()
            .expectStatus().isNoContent();

        // Проверяем, что больше не находится
        webTestClient.post()
            .uri("/auth/user/get-user")
            .header("id", tempId.toString())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void getAllUsers_NoFilters_ShouldReturnPage() {
        webTestClient.get()
            .uri("/auth/user/list-user")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content.length()").isNumber()
            .jsonPath("$.totalElements").isEqualTo(7); // у нас 6 пользователей в test-data
    }

    @Test
    void getAllUsers_WithNameFilter_ShouldReturnOnlyMatching() {
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/auth/user/list-user")
                .queryParam("name", "анна")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.content[0].name").isEqualTo("Анна");
    }

    @Test
    void getAllUsers_WithSurnameFilter_ShouldReturnOnlyMatching() {
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/auth/user/list-user")
                .queryParam("surname", "Сидорова")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content[0].surname").isEqualTo("Сидорова");
    }

    @Test
    void getAllUsers_WithBothFilters_ShouldReturnExactMatch() {
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/auth/user/list-user")
                .queryParam("name", "Екатерина")
                .queryParam("surname", "Васильева")
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(1)
            .jsonPath("$.content[0].active").isEqualTo("DELETED");
    }
}