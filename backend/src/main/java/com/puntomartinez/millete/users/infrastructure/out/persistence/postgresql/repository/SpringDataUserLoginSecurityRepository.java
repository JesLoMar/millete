package com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.repository;
import com.puntomartinez.millete.users.infrastructure.out.persistence.postgresql.entity.UserLoginSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface SpringDataUserLoginSecurityRepository extends JpaRepository<UserLoginSecurityEntity, UUID> {
}