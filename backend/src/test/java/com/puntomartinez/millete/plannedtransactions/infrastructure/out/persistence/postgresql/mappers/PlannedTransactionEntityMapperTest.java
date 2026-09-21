package com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction.FrequencyType;
import com.puntomartinez.millete.plannedtransactions.infrastructure.out.persistence.postgresql.entity.PlannedTransactionEntity;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PlannedTransactionEntityMapper")
class PlannedTransactionEntityMapperTest {

    private final PlannedTransactionEntityMapper mapper =
            Mappers.getMapper(PlannedTransactionEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 11, 30);

        PlannedTransaction domain = PlannedTransaction.reconstitute(
                id, userId, categoryId,
                new BigDecimal("123.45"),
                TransactionType.EXPENSE,
                "Rent",
                FrequencyType.MONTHS,
                1,
                LocalDate.of(2024, 1, 1),
                null,
                createdAt, modifiedAt,
                true, null, 0
        );

        PlannedTransactionEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getCategoryId()).isEqualTo(categoryId);
        assertThat(entity.getAmount()).isEqualByComparingTo("123.45");
        assertThat(entity.getType()).isEqualTo("EXPENSE");
        assertThat(entity.getDescription()).isEqualTo("Rent");
        assertThat(entity.getFrequencyType()).isEqualTo("MONTHS");
        assertThat(entity.getFrequencyInterval()).isEqualTo(1);
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getFailureCount()).isZero();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 11, 30);

        PlannedTransactionEntity entity = new PlannedTransactionEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCategoryId(categoryId);
        entity.setAmount(new BigDecimal("123.45"));
        entity.setType("EXPENSE");
        entity.setDescription("Rent");
        entity.setFrequencyType("MONTHS");
        entity.setFrequencyInterval(1);
        entity.setStartDate(LocalDate.of(2024, 1, 1));
        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);
        entity.setActive(true);
        entity.setLastExecutedDate(LocalDate.of(2024, 2, 1));
        entity.setFailureCount(2);

        PlannedTransaction domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getCategoryId()).isEqualTo(categoryId);
        assertThat(domain.getAmount()).isEqualByComparingTo("123.45");
        assertThat(domain.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(domain.getDescription()).isEqualTo("Rent");
        assertThat(domain.getFrequencyType()).isEqualTo(FrequencyType.MONTHS);
        assertThat(domain.getFrequencyInterval()).isEqualTo(1);
        assertThat(domain.getLastExecutedDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(domain.getFailureCount()).isEqualTo(2);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should return null for unmapped type strings")
    void shouldReturnNullForUnmappedTypeStrings() {
        PlannedTransactionEntity entity = new PlannedTransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setAmount(BigDecimal.TEN);
        entity.setType(null);
        entity.setDescription("Test");
        entity.setFrequencyType(null);
        entity.setFrequencyInterval(1);
        entity.setStartDate(LocalDate.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);
        entity.setFailureCount(0);

        PlannedTransaction domain = mapper.toDomain(entity);

        assertThat(domain.getType()).isNull();
        assertThat(domain.getFrequencyType()).isNull();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        PlannedTransaction original = PlannedTransaction.create(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("99.99"),
                TransactionType.INCOME,
                "Salary",
                FrequencyType.WEEKS,
                2,
                LocalDate.now(),
                null
        );

        PlannedTransactionEntity entity = mapper.toEntity(original);
        PlannedTransaction restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getCategoryId()).isEqualTo(original.getCategoryId());
        assertThat(restored.getAmount()).isEqualByComparingTo(original.getAmount());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.getDescription()).isEqualTo(original.getDescription());
        assertThat(restored.getFrequencyType()).isEqualTo(original.getFrequencyType());
        assertThat(restored.getFrequencyInterval()).isEqualTo(original.getFrequencyInterval());
        assertThat(restored.getStartDate()).isEqualTo(original.getStartDate());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}