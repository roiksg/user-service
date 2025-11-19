package com.example.user_service.dto;

import com.example.user_service.entity.enums.UserType;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsersUpdateRequest {
    String name;
    String surname;
    LocalDate birthDate;
    String email;
    UserType active;
}
