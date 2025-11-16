package com.example.user_service.mapper;

import com.example.user_service.dto.PaymentCardCreateRequest;
import com.example.user_service.dto.PaymentCardResponseDto;
import com.example.user_service.dto.PaymentCardUpdateRequest;
import com.example.user_service.entity.PaymentCard;
import com.example.user_service.entity.Users;
import com.example.user_service.entity.enums.PaymentCardType;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(target = "userId", source = "user.id")
    PaymentCardResponseDto toDto(PaymentCard entity);

    List<PaymentCardResponseDto> toDtoList(List<PaymentCard> entities);

    // toEntity: CreateRequest + Users → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "holder", ignore = true)
    @Mapping(target = "active", constant = "ACTIVE")
    @Mapping(target = "user", source = ".")
    PaymentCard toEntity(PaymentCardCreateRequest request, @Context Users user);

    // updateEntity: UpdateRequest → Entity (partial update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "active", source = "active", qualifiedByName = "stringToActive")
    void updateEntity(@MappingTarget PaymentCard entity, PaymentCardUpdateRequest request);

    // Конвертер String → PaymentCardType
    @Named("stringToActive")
    default PaymentCardType stringToActive(String active) {
        if (active == null || active.isBlank()) {
            return null;
        }
        try {
            return PaymentCardType.valueOf(active.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // или бросить ошибку
        }
    }

}
