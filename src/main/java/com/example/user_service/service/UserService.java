package com.example.user_service.service;

import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.UserType;
import com.example.user_service.repository.PaymentCardRepository;
import com.example.user_service.repository.UsersRepository;
import com.example.user_service.util.specification.UsersSpecification;
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
public class UserService {

    private final UsersRepository usersRepository;
    private final PaymentCardRepository paymentCardRepository;

    // Create User
    public Users createUser(Users user) {
        user.setId(null); // UUID задастся БД
        return usersRepository.save(user);
    }

    // Get User by ID
    public Users getUser(UUID id) {
        return usersRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
    }

    // Get All Users (paginated + filtered)
    public Page<Users> getAllUsers(String name, String surname, Pageable pageable) {
        Specification<Users> spec = Specification.where(
                UsersSpecification.firstNameContains(name))
            .and(UsersSpecification.surnameContains(surname));

        return usersRepository.findAll(spec, pageable);
    }

    // Update User
    public Users updateUser(UUID id, Users updated) {
        Users user = getUser(id);

        user.setName(updated.getName());
        user.setSurname(updated.getSurname());
        user.setBirthDate(updated.getBirthDate());
        user.setEmail(updated.getEmail());
        user.setActive(updated.getActive());

        return usersRepository.save(user);
    }

    // Activate/Deactivate User
    public Users changeStatus(UUID id, UserType status) {
        Users user = getUser(id);
        user.setActive(status);
        return usersRepository.save(user);
    }
}
