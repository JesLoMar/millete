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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("PlannedTransactionEntityMapper")
class PlannedTransactionEntityMapperTest {
    private final PlannedTransactionEntityMapper mapper =
            Mappers.getMapper(PlannedTransactionEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity with enums as strings")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        PlannedTransaction domain = PlannedTransaction.reconstitute(
                id, userId, categoryId,
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                "Rent",
                FrequencyType.MONTHS, 1,
                LocalDate.now(), null,
                createdAt, modifiedAt,
                true, null, 0
        );

        PlannedTransactionEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getCategoryId()).isEqualTo(categoryId);
        assertThat(entity.getAmount()).isEqualByComparingTo("100.00");
        assertThat(entity.getType()).isEqualTo("EXPENSE");
        assertThat(entity.getDescription()).isEqualTo("Rent");
        assertThat(entity.getFrequencyType()).isEqualTo("MONTHS");
        assertThat(entity.getFrequencyInterval()).isEqualTo(1);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain reconstituting aggregate")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        PlannedTransactionEntity entity = new PlannedTransactionEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCategoryId(categoryId);
        entity.setAmount(new BigDecimal("100.00"));
        entity.setType("EXPENSE");
        entity.setDescription("Rent");
        entity.setFrequencyType("MONTHS");
        entity.setFrequencyInterval(1);
        entity.setStartDate(LocalDate.now());
        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);
        entity.setActive(true);
        entity.setFailureCount(2);

        PlannedTransaction domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(domain.getFrequencyType()).isEqualTo(FrequencyType.MONTHS);
        assertThat(domain.getFailureCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return null when entity is null")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should preserve data in domain-entity-domain round trip")
    void shouldPreserveDataInRoundTrip() {
        PlannedTransaction original = PlannedTransaction.create(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("50.00"),
                TransactionType.INCOME, "Salary",
                FrequencyType.WEEKS, 2,
                LocalDate.now(), LocalDate.now().plusMonths(6)
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
        assertThat(restored.getEndDate()).isEqualTo(original.getEndDate());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }

    @Test
    @DisplayName("Should throw InvalidInputException when entity has null enums")
    void shouldThrowWhenEntityHasNullEnums() {
        PlannedTransactionEntity entity = new PlannedTransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(UUID.randomUUID());
        entity.setAmount(new BigDecimal("10.00"));
        entity.setType(null);
        entity.setDescription("Desc");
        entity.setFrequencyType(null);
        entity.setFrequencyInterval(1);
        entity.setStartDate(LocalDate.now());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setModifiedAt(LocalDateTime.now());
        entity.setActive(true);
        entity.setFailureCount(0);

        assertThatThrownBy(() -> mapper.toDomain(entity))
                .isInstanceOf(
                        com.puntomartinez.millete.shared.domain.exception.InvalidInputException.class
                );
    }

    @Test
    @DisplayName("Should map enums using named helper methods")
    void shouldMapEnumsViaHelpers() {
        assertThat(mapper.mapTransactionTypeToString(TransactionType.EXPENSE))
                .isEqualTo("EXPENSE");
        assertThat(mapper.mapTransactionTypeToString(null)).isNull();
        assertThat(mapper.mapFrequencyTypeToString(FrequencyType.MONTHS))
                .isEqualTo("MONTHS");
        assertThat(mapper.mapFrequencyTypeToString(null)).isNull();
    }
}