package com.example.user_service.service;

import com.example.user_service.dto.UsersCreateRequest;
import com.example.user_service.dto.UsersResponseDto;
import com.example.user_service.dto.UsersUpdateRequest;
import com.example.user_service.entity.enums.UserType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UsersResponseDto createUser(UsersCreateRequest user);
    UsersResponseDto getUser(UUID id);
    Page<UsersResponseDto> getAllUsers(String name, String surname, Pageable pageable);
    UsersResponseDto updateUser(UUID id, UsersUpdateRequest usersUpdateRequest);
    UsersResponseDto changeStatus(UUID id, UserType status);
    void removeUser (UUID id);


}
