package com.example.user_service.dto;

import com.example.user_service.entity.enums.UserType;
import com.example.user_service.util.validation.RegExp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
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
public class UsersResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    UUID id;
    @NotBlank
    String name;
    @NotBlank
    String surname;
    @NotNull
    LocalDate birthDate;
    @NotBlank(message = "email не иожет быть пустым")
    @Pattern(message = "Некорректный email", regexp = RegExp.EMAIL_REG)
    String email;
    @NotNull
    UserType active;
    List<PaymentCardResponseDto> cards;
}
