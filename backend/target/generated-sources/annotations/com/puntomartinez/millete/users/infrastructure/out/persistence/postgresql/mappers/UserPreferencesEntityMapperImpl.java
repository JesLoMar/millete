package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.UserPreferences;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserPreferencesEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:14+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class UserPreferencesEntityMapperImpl implements UserPreferencesEntityMapper {

    @Override
    public UserPreferencesEntity toEntity(UserPreferences domain) {
        if ( domain == null ) {
            return null;
        }

        UserPreferencesEntity userPreferencesEntity = new UserPreferencesEntity();

        userPreferencesEntity.setId( domain.getId() );
        userPreferencesEntity.setUserId( domain.getUserId() );
        Map<String, Object> map = domain.getPreferences();
        if ( map != null ) {
            userPreferencesEntity.setPreferences( new LinkedHashMap<String, Object>( map ) );
        }
        userPreferencesEntity.setCreatedAt( domain.getCreatedAt() );
        userPreferencesEntity.setModifiedAt( domain.getModifiedAt() );

        return userPreferencesEntity;
    }

    @Override
    public UserPreferences toDomain(UserPreferencesEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserPreferences userPreferences = new UserPreferences();

        userPreferences.setId( entity.getId() );
        userPreferences.setUserId( entity.getUserId() );
        Map<String, Object> map = entity.getPreferences();
        if ( map != null ) {
            userPreferences.setPreferences( new LinkedHashMap<String, Object>( map ) );
        }
        userPreferences.setCreatedAt( entity.getCreatedAt() );
        userPreferences.setModifiedAt( entity.getModifiedAt() );

        return userPreferences;
    }
}
