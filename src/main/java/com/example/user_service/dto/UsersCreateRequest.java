package com.example.user_service.dto;

import com.example.user_service.util.validation.RegExp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.UUID;
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
public class UsersCreateRequest{
    @NotNull
    UUID id;
    @NotBlank
    String name;
    @NotBlank
    String surname;
    @NotNull
    LocalDate birthDate;
    @NotBlank (message = "email не иожет быть пустым")
    @Pattern(message = "Некорректный email", regexp = RegExp.EMAIL_REG)
    String email;
}
