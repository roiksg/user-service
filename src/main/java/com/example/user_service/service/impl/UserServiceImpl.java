package com.example.user_service.service.impl;

import static com.example.user_service.util.ExceptionMessage.USER_NOT_FOUND_BY_ID;

import com.example.user_service.dto.UsersCreateRequest;
import com.example.user_service.dto.UsersResponseDto;
import com.example.user_service.dto.UsersUpdateRequest;
import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.UserType;
import com.example.user_service.exception.NotFoundException;
import com.example.user_service.mapper.UsersMapper;
import com.example.user_service.repository.UsersRepository;
import com.example.user_service.service.UserService;
import com.example.user_service.util.specification.UsersSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    // Create User
    @Override
    @CachePut(value = "users", key = "#result.id")
    public UsersResponseDto createUser(UsersCreateRequest user) {
        Users userEntity = usersMapper.toEntity(user);
        return usersMapper.toDto(usersRepository.save(userEntity));
    }

    // Get User by ID
    @Override
    @Cacheable(value = "users", key = "#id")
    public UsersResponseDto getUser(UUID id) {
        Users users = usersRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_BY_ID.getDescription()));
        return usersMapper.toDto(users);
    }

    // Get All Users (paginated + filtered)
    @Override
    public Page<UsersResponseDto> getAllUsers(String name, String surname, Pageable pageable) {
        Specification<Users> spec = Specification.where(
                UsersSpecification.firstNameContains(name))
            .and(UsersSpecification.surnameContains(surname));

        Page<Users> usersPage = usersRepository.findAll(spec, pageable);

        return usersPage.map(usersMapper::toDto);
    }

    // Update User
    @Override
    @CachePut(value = "users", key = "#id")
    public UsersResponseDto updateUser(UUID id, UsersUpdateRequest usersUpdateRequest) {
        Users users = usersRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_BY_ID.getDescription()));

        usersMapper.updateEntity(users, usersUpdateRequest);
        usersRepository.save(users);

        return usersMapper.toDto(users);
    }

    // Activate/Deactivate User
    @Override
    @CachePut(value = "users", key = "#id")
    public UsersResponseDto changeStatus(UUID id, UserType status) {
        Users user = usersRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_BY_ID.getDescription()));
        user.setActive(status);
        return usersMapper.toDto(user);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void removeUser (UUID id) {
        Users user = usersRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_BY_ID.getDescription()));
        usersRepository.delete(user);
    }
}
