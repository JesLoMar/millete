package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {
    UserEntity toEntity(User domain);
    User toDomain(UserEntity entity);
}