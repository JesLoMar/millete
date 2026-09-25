package com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import com.puntomartinez.millete.savingsgoals.infrastructure.out.persistence.postgresql.entity.SavingsGoalEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import com.puntomartinez.millete.shared.domain.time.FixedTimeProvider;
import java.time.Instant;

@DisplayName("SavingsGoalEntityMapper")
class SavingsGoalEntityMapperTest {
    static final TimeProvider TIME = new FixedTimeProvider(
            Instant.parse("2024-01-15T10:00:00Z"));


    private final SavingsGoalEntityMapper mapper =
            Mappers.getMapper(SavingsGoalEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 11, 30);
        LocalDate deadline = LocalDate.of(2030, 6, 1);

        SavingsGoal domain = SavingsGoal.reconstitute(
                id,
                userId,
                "Vacation",
                new BigDecimal("1000.00"),
                new BigDecimal("250.00"),
                deadline,
                GoalPriority.HIGH,
                "https://example.com",
                createdAt,
                modifiedAt,
                true
        );

        SavingsGoalEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getName()).isEqualTo("Vacation");
        assertThat(entity.getTargetAmount()).isEqualByComparingTo("1000.00");
        assertThat(entity.getCurrentAmount()).isEqualByComparingTo("250.00");
        assertThat(entity.getDeadline()).isEqualTo(deadline);
        assertThat(entity.getPriority()).isEqualTo("HIGH");
        assertThat(entity.getLink()).isEqualTo("https://example.com");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getModifiedAt()).isEqualTo(modifiedAt);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 11, 30);
        LocalDate deadline = LocalDate.of(2030, 6, 1);

        SavingsGoalEntity entity = new SavingsGoalEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setName("Vacation");
        entity.setTargetAmount(new BigDecimal("1000.00"));
        entity.setCurrentAmount(new BigDecimal("250.00"));
        entity.setDeadline(deadline);
        entity.setPriority("HIGH");
        entity.setLink("https://example.com");
        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);
        entity.setActive(true);

        SavingsGoal domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getName()).isEqualTo("Vacation");
        assertThat(domain.getTargetAmount()).isEqualByComparingTo("1000.00");
        assertThat(domain.getCurrentAmount()).isEqualByComparingTo("250.00");
        assertThat(domain.getDeadline()).isEqualTo(deadline);
        assertThat(domain.getPriority()).isEqualTo(GoalPriority.HIGH);
        assertThat(domain.getLink()).isEqualTo("https://example.com");
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getModifiedAt()).isEqualTo(modifiedAt);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should return null when domain is null")
    void shouldReturnNullWhenDomainIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        SavingsGoal original = SavingsGoal.create(TIME, 
                UUID.randomUUID(),
                "Vacation",
                new BigDecimal("1000.00"),
                LocalDate.now().plusDays(30),
                GoalPriority.HIGH,
                "https://example.com"
        );

        SavingsGoalEntity entity = mapper.toEntity(original);
        SavingsGoal restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getName()).isEqualTo(original.getName());
        assertThat(restored.getTargetAmount()).isEqualByComparingTo(original.getTargetAmount());
        assertThat(restored.getCurrentAmount()).isEqualByComparingTo(original.getCurrentAmount());
        assertThat(restored.getDeadline()).isEqualTo(original.getDeadline());
        assertThat(restored.getPriority()).isEqualTo(original.getPriority());
        assertThat(restored.getLink()).isEqualTo(original.getLink());
        assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(restored.getModifiedAt()).isEqualTo(original.getModifiedAt());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }

    @Test
    @DisplayName("Should convert priority to string")
    void shouldConvertPriorityToString() {
        assertThat(mapper.priorityToString(GoalPriority.HIGH)).isEqualTo("HIGH");
        assertThat(mapper.priorityToString(GoalPriority.MEDIUM)).isEqualTo("MEDIUM");
        assertThat(mapper.priorityToString(GoalPriority.LOW)).isEqualTo("LOW");
        assertThat(mapper.priorityToString(null)).isNull();
    }

    @Test
    @DisplayName("Should convert string to priority")
    void shouldConvertStringToPriority() {
        assertThat(mapper.stringToPriority("HIGH")).isEqualTo(GoalPriority.HIGH);
        assertThat(mapper.stringToPriority("MEDIUM")).isEqualTo(GoalPriority.MEDIUM);
        assertThat(mapper.stringToPriority("LOW")).isEqualTo(GoalPriority.LOW);
    }

    @Test
    @DisplayName("Should default to MEDIUM when priority string is null or blank")
    void shouldDefaultToMediumWhenPriorityStringIsNullOrBlank() {
        assertThat(mapper.stringToPriority(null)).isEqualTo(GoalPriority.MEDIUM);
        assertThat(mapper.stringToPriority("")).isEqualTo(GoalPriority.MEDIUM);
        assertThat(mapper.stringToPriority("   ")).isEqualTo(GoalPriority.MEDIUM);
    }

    @Test
    @DisplayName("Should default to MEDIUM when priority string is invalid")
    void shouldDefaultToMediumWhenPriorityStringIsInvalid() {
        assertThat(mapper.stringToPriority("INVALID")).isEqualTo(GoalPriority.MEDIUM);
        assertThat(mapper.stringToPriority("high")).isEqualTo(GoalPriority.MEDIUM);
    }
}