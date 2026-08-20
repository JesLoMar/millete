package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.adapters;
import com.puntomartinez.millete.users.domain.model.UserLoginSecurity;
import com.puntomartinez.millete.users.domain.ports.out.LoginSecurityRepository;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserLoginSecurityEntity;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.mappers.UserLoginSecurityEntityMapper;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository.SpringDataUserLoginSecurityRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
@Component
public class UserLoginSecurityPostgresAdapter implements LoginSecurityRepository {
    private final SpringDataUserLoginSecurityRepository repository;
    private final UserLoginSecurityEntityMapper mapper;
    public UserLoginSecurityPostgresAdapter(SpringDataUserLoginSecurityRepository repository,
                                            UserLoginSecurityEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    @Override
    public Optional<UserLoginSecurity> findByUserId(UUID userId) {
        return repository.findById(userId).map(mapper::toDomain);
    }
    @Override
    public UserLoginSecurity save(UserLoginSecurity security) {
        UserLoginSecurityEntity entity = mapper.toEntity(security);
        return mapper.toDomain(repository.save(entity));
    }
}