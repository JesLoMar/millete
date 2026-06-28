package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserPreferencesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserPreferencesEntityMapper {
    UserPreferencesEntity toEntity(UserPreferences domain);
    UserPreferences toDomain(UserPreferencesEntity entity);
}
