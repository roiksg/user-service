package com.example.user_service.mapper;

import com.example.user_service.dto.UsersCreateRequest;
import com.example.user_service.dto.UsersResponseDto;
import com.example.user_service.dto.UsersUpdateRequest;
import com.example.user_service.entity.Users;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {PaymentCardMapper.class})
public interface UsersMapper {

    @Mapping(target = "active", source = "active")
    @Mapping(target = "birthDate", source = "birthDate")
    UsersResponseDto toDto(Users entity);

    List<UsersResponseDto> toDtoList(List<Users> entities);

    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "active", expression = "java(com.example.user_service.entity.enums.UserType.ACTIVE)")
    Users toEntity(UsersCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Users entity, UsersUpdateRequest request);
}
