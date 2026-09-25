package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.UserSession;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserSessionEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:16+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class UserSessionEntityMapperImpl implements UserSessionEntityMapper {

    @Override
    public UserSessionEntity toEntity(UserSession domain) {
        if ( domain == null ) {
            return null;
        }

        UserSessionEntity.UserSessionEntityBuilder userSessionEntity = UserSessionEntity.builder();

        userSessionEntity.id( domain.getId() );
        userSessionEntity.userId( domain.getUserId() );
        userSessionEntity.channel( domain.getChannel() );
        userSessionEntity.createdAt( domain.getCreatedAt() );
        userSessionEntity.active( domain.isActive() );
        userSessionEntity.modifiedAt( domain.getModifiedAt() );

        return userSessionEntity.build();
    }

    @Override
    public UserSession toDomain(UserSessionEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserSession userSession = new UserSession();

        userSession.setId( entity.getId() );
        userSession.setUserId( entity.getUserId() );
        userSession.setChannel( entity.getChannel() );
        userSession.setCreatedAt( entity.getCreatedAt() );
        userSession.setModifiedAt( entity.getModifiedAt() );
        userSession.setActive( entity.isActive() );

        return userSession;
    }
}
