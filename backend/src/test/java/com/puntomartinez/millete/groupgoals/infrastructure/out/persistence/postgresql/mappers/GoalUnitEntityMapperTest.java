package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.entity.GoalUnitEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoalUnitEntityMapper")
class GoalUnitEntityMapperTest {

    private final GoalUnitEntityMapper mapper =
            Mappers.getMapper(GoalUnitEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        GoalUnit domain = GoalUnit.create(
                "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE
        );

        GoalUnitEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(domain.getId());
        assertThat(entity.getName()).isEqualTo("Family trip");
        assertThat(entity.getMonthlyTarget()).isEqualByComparingTo("300.00");
        assertThat(entity.getDistributionMode()).isEqualTo("EQUITATIVE");
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        GoalUnitEntity entity = new GoalUnitEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Family trip");
        entity.setMonthlyTarget(new BigDecimal("300.00"));
        entity.setDistributionMode("EQUITATIVE");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);

        GoalUnit domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getName()).isEqualTo("Family trip");
        assertThat(domain.getMonthlyTarget()).isEqualByComparingTo("300.00");
        assertThat(domain.getDistributionMode()).isEqualTo(DistributionMode.EQUITATIVE);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        GoalUnit original = GoalUnit.create(
                "Family trip", new BigDecimal("300.00"),
                DistributionMode.PROPORTIONAL
        );

        GoalUnitEntity entity = mapper.toEntity(original);
        GoalUnit restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getName()).isEqualTo(original.getName());
        assertThat(restored.getMonthlyTarget())
                .isEqualByComparingTo(original.getMonthlyTarget());
        assertThat(restored.getDistributionMode())
                .isEqualTo(original.getDistributionMode());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}