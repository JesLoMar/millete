package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserEntity;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class UserEntityMapperImpl implements UserEntityMapper {

    @Override
    public UserEntity toEntity(User domain) {
        if ( domain == null ) {
            return null;
        }

        UserEntity userEntity = new UserEntity();

        userEntity.setId( domain.getId() );
        userEntity.setUsername( domain.getUsername() );
        userEntity.setEmail( domain.getEmail() );
        userEntity.setPassword( domain.getPassword() );
        userEntity.setCreatedAt( domain.getCreatedAt() );
        userEntity.setModifiedAt( domain.getModifiedAt() );
        userEntity.setActive( domain.isActive() );
        userEntity.setAnonymized( domain.isAnonymized() );

        return userEntity;
    }

    @Override
    public User toDomain(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UUID id = null;
        String username = null;
        String email = null;
        String password = null;
        Instant createdAt = null;
        Instant modifiedAt = null;
        boolean active = false;
        boolean anonymized = false;

        id = entity.getId();
        username = entity.getUsername();
        email = entity.getEmail();
        password = entity.getPassword();
        createdAt = entity.getCreatedAt();
        modifiedAt = entity.getModifiedAt();
        active = entity.isActive();
        anonymized = entity.isAnonymized();

        User user = new User( id, username, email, password, createdAt, modifiedAt, active, anonymized );

        return user;
    }
}
