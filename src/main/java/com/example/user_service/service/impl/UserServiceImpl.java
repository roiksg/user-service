package com.example.user_service.service.impl;

import com.example.user_service.dto.UsersCreateRequest;
import com.example.user_service.dto.UsersResponseDto;
import com.example.user_service.dto.UsersUpdateRequest;
import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.UserType;
import com.example.user_service.mapper.UsersMapper;
import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UsersRepository;
import com.example.user_service.service.UserService;
import com.example.user_service.util.specification.UsersSpecification;
import java.util.NoSuchElementException;
import java.util.Optional;
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
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final UsersMapper usersMapper;

    // Create User
    @Override
    public UsersResponseDto createUser(UsersCreateRequest user) {
        Users userEntity = usersMapper.toEntity(user);
        UsersResponseDto responseDto = usersMapper.toDto(usersRepository.save(userEntity));
        return responseDto;
    }

    // Get User by ID
    @Override
    public UsersResponseDto getUser(UUID id) {
        Users users = usersRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        UsersResponseDto usersResponseDto = usersMapper.toDto(users);
        return usersResponseDto;
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
    public UsersResponseDto updateUser(UUID id, UsersUpdateRequest usersUpdateRequest) {
        Optional<Users> usersOptional = usersRepository.findById(id);
        Users users = usersOptional.get();

        usersMapper.updateEntity(users, usersUpdateRequest);
        usersRepository.save(users);
        UsersResponseDto usersResponseDto = usersMapper.toDto(users);

        return usersResponseDto;
    }

    // Activate/Deactivate User
    @Override
    public UsersResponseDto changeStatus(UUID id, UserType status) {
        Optional<Users> userOptional = usersRepository.findById(id);
        Users user = userOptional.get();
        user.setActive(status);
        UsersResponseDto usersResponseDto = usersMapper.toDto(user);
        return usersResponseDto;
    }
}
