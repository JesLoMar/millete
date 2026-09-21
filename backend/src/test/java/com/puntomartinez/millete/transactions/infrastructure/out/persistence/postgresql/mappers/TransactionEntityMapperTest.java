package com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.transactions.infrastructure.out.persistence.postgresql.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionEntityMapper")
class TransactionEntityMapperTest {

    private TransactionEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionEntityMapper() {};
    }

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 11, 30);

        Transaction domain = Transaction.reconstitute(
                id, userId, categoryId,
                new BigDecimal("123.45"),
                LocalDateTime.of(2024, 1, 1, 9, 0),
                TransactionType.EXPENSE,
                "Groceries",
                createdAt, modifiedAt, true
        );

        TransactionEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getCategoryId()).isEqualTo(categoryId);
        assertThat(entity.getAmount()).isEqualByComparingTo("123.45");
        assertThat(entity.getType()).isEqualTo("EXPENSE");
        assertThat(entity.getDescription()).isEqualTo("Groceries");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getModifiedAt()).isEqualTo(modifiedAt);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 11, 30);
        LocalDateTime date = LocalDateTime.of(2024, 1, 1, 9, 0);

        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCategoryId(categoryId);
        entity.setAmount(new BigDecimal("123.45"));
        entity.setDate(date);
        entity.setType("EXPENSE");
        entity.setDescription("Groceries");
        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);
        entity.setActive(true);

        Transaction domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getCategoryId()).isEqualTo(categoryId);
        assertThat(domain.getAmount()).isEqualByComparingTo("123.45");
        assertThat(domain.getDate()).isEqualTo(date);
        assertThat(domain.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(domain.getDescription()).isEqualTo("Groceries");
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
        Transaction original = Transaction.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("99.99"),
                LocalDateTime.now(),
                TransactionType.INCOME,
                "Salary"
        );

        TransactionEntity entity = mapper.toEntity(original);
        Transaction restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getCategoryId()).isEqualTo(original.getCategoryId());
        assertThat(restored.getAmount()).isEqualByComparingTo(original.getAmount());
        assertThat(restored.getDate()).isEqualTo(original.getDate());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.getDescription()).isEqualTo(original.getDescription());
        assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(restored.getModifiedAt()).isEqualTo(original.getModifiedAt());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}