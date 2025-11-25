package com.example.user_service.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.user_service.dto.ChangeUserStatusRequest;
import com.example.user_service.dto.UsersCreateRequest;
import com.example.user_service.dto.UsersResponseDto;
import com.example.user_service.dto.UsersUpdateRequest;
import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.UserType;
import com.example.user_service.exception.NotFoundException;
import com.example.user_service.mapper.UsersMapper;
import com.example.user_service.repository.UsersRepository;
import com.example.user_service.service.impl.UserServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UsersMapper usersMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private static UUID EXISTING_ID;
    private static UUID NON_EXISTING_ID;

    private Users existingUser;
    private UsersResponseDto existingUserDto;

    @BeforeAll
    static void init() {
        EXISTING_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        NON_EXISTING_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    }

    @BeforeEach
    void setUp() {
        existingUser = Users.builder()
            .id(EXISTING_ID)
            .name("Иван")
            .surname("Иванов")
            .birthDate(LocalDate.of(1990, 5, 15))
            .email("ivan@example.com")
            .active(UserType.ACTIVE)
            .cards(List.of())
            .build();

        existingUserDto = UsersResponseDto.builder()
            .id(EXISTING_ID)
            .name("Иван")
            .surname("Иванов")
            .birthDate(LocalDate.of(1990, 5, 15))
            .email("ivan@example.com")
            .active(UserType.ACTIVE)
            .cards(List.of())
            .build();
    }

    // ===================== CREATE USER =====================
    @Test
    void createUser_Success() {
        var request = UsersCreateRequest.builder()
            .id(EXISTING_ID)
            .name("Иван")
            .surname("Иванов")
            .birthDate(LocalDate.now().minusYears(30))
            .email("ivan@example.com")
            .build();

        when(usersMapper.toEntity(request)).thenReturn(existingUser);
        when(usersRepository.save(existingUser)).thenReturn(existingUser);
        when(usersMapper.toDto(existingUser)).thenReturn(existingUserDto);

        UsersResponseDto result = userService.createUser(request);

        assertEquals(existingUserDto, result);
        verify(usersMapper).toEntity(request);
        verify(usersRepository).save(existingUser);
        verify(usersMapper).toDto(existingUser);
    }

    // ===================== GET USER BY ID =====================
    @Test
    void getUser_WhenExists_ReturnsDto() {
        when(usersRepository.findById(EXISTING_ID)).thenReturn(Optional.of(existingUser));
        when(usersMapper.toDto(existingUser)).thenReturn(existingUserDto);

        UsersResponseDto result = userService.getUser(EXISTING_ID);

        assertEquals(existingUserDto, result);
        verify(usersRepository).findById(EXISTING_ID);
    }

    @Test
    void getUser_WhenNotExists_ThrowsNotFoundException() {
        when(usersRepository.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
            () -> userService.getUser(NON_EXISTING_ID));

        assertTrue(exception.getMessage().contains("Пользователя с таким ID не существует"));
        verify(usersRepository).findById(NON_EXISTING_ID);
    }

    // ===================== GET ALL USERS =====================
    @ParameterizedTest
    @MethodSource("getAllUsersArguments")
    void getAllUsers_ReturnsFilteredPage(String name, String surname) {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(existingUser), pageable, 1);
        var expectedPage = new PageImpl<>(List.of(existingUserDto), pageable, 1);

        when(usersRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(usersMapper.toDto(existingUser)).thenReturn(existingUserDto);

        Page<UsersResponseDto> result = userService.getAllUsers(name, surname, pageable);

        assertEquals(expectedPage.getTotalElements(), result.getTotalElements());
        assertEquals(expectedPage.getContent().get(0), result.getContent().get(0));
        verify(usersRepository).findAll(any(Specification.class), eq(pageable));
    }

    private static Stream<Arguments> getAllUsersArguments() {
        return Stream.of(
            Arguments.of("Иван", null),
            Arguments.of(null, "Иванов"),
            Arguments.of("Иван", "Иванов"),
            Arguments.of(null, null)
        );
    }

    // ===================== UPDATE USER =====================
    @Test
    void updateUser_WhenExists_UpdatesFields() {
        var request = UsersUpdateRequest.builder()
            .name("Пётр")
            .email("petr@example.com")
            .build();

        var updatedDto = existingUserDto.toBuilder()
            .name("Пётр")
            .email("petr@example.com")
            .build();

        when(usersRepository.findById(EXISTING_ID)).thenReturn(Optional.of(existingUser));
        doNothing().when(usersMapper).updateEntity(existingUser, request);
        when(usersRepository.save(existingUser)).thenReturn(existingUser);
        when(usersMapper.toDto(existingUser)).thenReturn(updatedDto);

        UsersResponseDto result = userService.updateUser(EXISTING_ID, request);

        assertEquals("Пётр", result.getName());
        assertEquals("petr@example.com", result.getEmail());
    }

    @Test
    void updateUser_WhenNotExists_ThrowsNotFoundException() {
        when(usersRepository.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.updateUser(NON_EXISTING_ID, UsersUpdateRequest.builder().build()));
    }

    // ===================== CHANGE STATUS =====================
    @Test
    void changeStatus_WhenExists_ChangesStatus() {
        var frozenDto = existingUserDto.toBuilder()
            .active(UserType.FROZEN)
            .build();
        ChangeUserStatusRequest statusRequest = ChangeUserStatusRequest.builder().status(UserType.FROZEN).build();

        when(usersRepository.findById(EXISTING_ID)).thenReturn(Optional.of(existingUser));
        when(usersMapper.toDto(existingUser)).thenReturn(frozenDto);

        UsersResponseDto result = userService.changeStatus(EXISTING_ID, statusRequest);

        assertEquals(UserType.FROZEN, existingUser.getActive());
        assertEquals(UserType.FROZEN, result.getActive());
    }

    @Test
    void changeStatus_WhenNotExists_ThrowsNotFoundException() {
        ChangeUserStatusRequest statusRequest = ChangeUserStatusRequest.builder().status(UserType.FROZEN).build();
        when(usersRepository.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.changeStatus(NON_EXISTING_ID, statusRequest));
    }

    // ===================== REMOVE USER =====================
    @Test
    void removeUser_WhenExists_DeletesUser() {
        when(usersRepository.findById(EXISTING_ID)).thenReturn(Optional.of(existingUser));

        userService.removeUser(EXISTING_ID);

        verify(usersRepository).delete(existingUser);
    }

    @Test
    void removeUser_WhenNotExists_ThrowsNotFoundException() {
        when(usersRepository.findById(NON_EXISTING_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.removeUser(NON_EXISTING_ID));
    }
}