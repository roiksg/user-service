package com.example.user_service.controller;

import com.example.user_service.dto.ChangeUserStatusRequest;
import com.example.user_service.dto.UsersCreateRequest;
import com.example.user_service.dto.UsersResponseDto;
import com.example.user_service.dto.UsersUpdateRequest;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/auth/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/create-user")
    @ResponseStatus(HttpStatus.CREATED)
    public UsersResponseDto createUser (@RequestBody @Valid UsersCreateRequest userCreateRequest) {
        return userService.createUser(userCreateRequest);
    }

    @PostMapping("/get-user")
    @ResponseStatus(HttpStatus.OK)
    public UsersResponseDto getUser(@RequestHeader UUID id) {
        return userService.getUser(id);
    }

    @PostMapping("/update-user")
    @ResponseStatus(HttpStatus.OK)
    public UsersResponseDto updateUser(@RequestHeader UUID id, @RequestBody @Valid UsersUpdateRequest usersUpdateRequest) {
        return userService.updateUser(id, usersUpdateRequest);
    }

    @PostMapping("/change-user-status")
    @ResponseStatus(HttpStatus.OK)
    public UsersResponseDto changeStatus(@RequestHeader UUID id, @RequestBody @Valid ChangeUserStatusRequest status) {
        return userService.changeStatus(id, status);
    }

    @PostMapping("/remove-user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser (@RequestHeader UUID id) {
        userService.removeUser(id);
    }

    @GetMapping("/list-user")
    @ResponseStatus(HttpStatus.OK)
    public Page<UsersResponseDto> getAllUsers(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return userService.getAllUsers(name, surname, pageable);
    }
}
