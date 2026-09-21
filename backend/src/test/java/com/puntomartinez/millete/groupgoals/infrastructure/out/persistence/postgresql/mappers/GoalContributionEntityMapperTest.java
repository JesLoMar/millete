package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.ContributionType;
import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalContributionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoalContributionEntityMapper")
class GoalContributionEntityMapperTest {

    private final GoalContributionEntityMapper mapper =
            Mappers.getMapper(GoalContributionEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        GoalContribution domain = GoalContribution.create(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("100.00"),
                ContributionType.DEPOSIT, LocalDateTime.now()
        );

        GoalContributionEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getAmount()).isEqualByComparingTo("100.00");
        assertThat(entity.getType()).isEqualTo("DEPOSIT");
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        GoalContributionEntity entity = new GoalContributionEntity();
        entity.setId(UUID.randomUUID());
        entity.setGoalId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setAmount(new BigDecimal("100.00"));
        entity.setType("DEPOSIT");
        entity.setDate(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);

        GoalContribution domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getAmount()).isEqualByComparingTo("100.00");
        assertThat(domain.getType()).isEqualTo(ContributionType.DEPOSIT);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        GoalContribution original = GoalContribution.create(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("250.00"),
                ContributionType.WITHDRAWAL, LocalDateTime.now()
        );

        GoalContributionEntity entity = mapper.toEntity(original);
        GoalContribution restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getGoalId()).isEqualTo(original.getGoalId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getAmount())
                .isEqualByComparingTo(original.getAmount());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}