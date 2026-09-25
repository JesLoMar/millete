package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserLoginSecurityEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserLoginSecurityEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.JpaUserLoginSecurityRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserLoginSecurityPostgresAdapter")
class UserLoginSecurityPostgresAdapterTest {

    @Mock
    private JpaUserLoginSecurityRepository repository;

    @Mock
    private UserLoginSecurityEntityMapper mapper;

    @InjectMocks
    private UserLoginSecurityPostgresAdapter adapter;

    @Test
    @DisplayName("Should find by userId and map to domain")
    void shouldFindByUserId() {
        UUID userId = UUID.randomUUID();
        UserLoginSecurityEntity entity = new UserLoginSecurityEntity();
        entity.setUserId(userId);
        entity.setFailedAttempts(3);

        UserLoginSecurity domain = new UserLoginSecurity();
        domain.setUserId(userId);
        domain.setFailedAttempts(3);

        when(repository.findById(userId))
                .thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<UserLoginSecurity> result =
                adapter.findByUserId(userId);

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("Should save and map back to domain")
    void shouldSave() {
        UserLoginSecurity domain = new UserLoginSecurity();
        domain.setUserId(UUID.randomUUID());
        domain.setFailedAttempts(2);

        UserLoginSecurityEntity entity = new UserLoginSecurityEntity();
        UserLoginSecurityEntity savedEntity = new UserLoginSecurityEntity();
        UserLoginSecurity savedDomain = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

        UserLoginSecurity result = adapter.save(domain);

        assertThat(result).isSameAs(savedDomain);
        verify(mapper).toEntity(domain);
        verify(repository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }
}