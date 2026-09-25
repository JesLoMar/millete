package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserLoginSecurityEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:16+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class UserLoginSecurityEntityMapperImpl implements UserLoginSecurityEntityMapper {

    @Override
    public UserLoginSecurityEntity toEntity(UserLoginSecurity domain) {
        if ( domain == null ) {
            return null;
        }

        UserLoginSecurityEntity.UserLoginSecurityEntityBuilder userLoginSecurityEntity = UserLoginSecurityEntity.builder();

        userLoginSecurityEntity.userId( domain.getUserId() );
        userLoginSecurityEntity.failedAttempts( domain.getFailedAttempts() );
        userLoginSecurityEntity.blockedUntil( domain.getBlockedUntil() );
        userLoginSecurityEntity.lastAttemptAt( domain.getLastAttemptAt() );
        userLoginSecurityEntity.createdAt( domain.getCreatedAt() );
        userLoginSecurityEntity.modifiedAt( domain.getModifiedAt() );

        return userLoginSecurityEntity.build();
    }

    @Override
    public UserLoginSecurity toDomain(UserLoginSecurityEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserLoginSecurity userLoginSecurity = new UserLoginSecurity();

        userLoginSecurity.setUserId( entity.getUserId() );
        userLoginSecurity.setFailedAttempts( entity.getFailedAttempts() );
        userLoginSecurity.setBlockedUntil( entity.getBlockedUntil() );
        userLoginSecurity.setLastAttemptAt( entity.getLastAttemptAt() );
        userLoginSecurity.setCreatedAt( entity.getCreatedAt() );
        userLoginSecurity.setModifiedAt( entity.getModifiedAt() );

        return userLoginSecurity;
    }
}
