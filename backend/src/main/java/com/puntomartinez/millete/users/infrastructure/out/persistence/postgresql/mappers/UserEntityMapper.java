package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity toEntity(User domain);
    User toDomain(UserEntity entity);
}