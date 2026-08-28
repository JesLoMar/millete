package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;
import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserLoginSecurityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserLoginSecurityEntityMapper {
    UserLoginSecurityEntity toEntity(UserLoginSecurity domain);
    UserLoginSecurity toDomain(UserLoginSecurityEntity entity);
}