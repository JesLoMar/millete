package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.model.Investment.InvestmentType;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvestmentEntityMapper")
class InvestmentEntityMapperTest {

    private final InvestmentEntityMapper mapper =
            Mappers.getMapper(InvestmentEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime purchaseDate = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        Investment domain = Investment.reconstitute(
                id, userId, "Apple Inc.", "AAPL",
                new BigDecimal("10"), new BigDecimal("150.00"),
                new BigDecimal("180.00"),
                InvestmentType.STOCK, purchaseDate,
                createdAt, modifiedAt, true
        );

        InvestmentEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getAssetName()).isEqualTo("Apple Inc.");
        assertThat(entity.getTicker()).isEqualTo("AAPL");
        assertThat(entity.getQuantity()).isEqualByComparingTo("10");
        assertThat(entity.getPurchasePrice()).isEqualByComparingTo("150.00");
        assertThat(entity.getCurrentPrice()).isEqualByComparingTo("180.00");
        assertThat(entity.getType()).isEqualTo("STOCK");
        assertThat(entity.getPurchaseDate()).isEqualTo(purchaseDate);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime purchaseDate = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 10, 0);

        InvestmentEntity entity = new InvestmentEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setAssetName("Apple Inc.");
        entity.setTicker("AAPL");
        entity.setQuantity(new BigDecimal("10"));
        entity.setPurchasePrice(new BigDecimal("150.00"));
        entity.setCurrentPrice(new BigDecimal("180.00"));
        entity.setType("STOCK");
        entity.setPurchaseDate(purchaseDate);
        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);
        entity.setActive(true);

        Investment domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getAssetName()).isEqualTo("Apple Inc.");
        assertThat(domain.getTicker()).isEqualTo("AAPL");
        assertThat(domain.getType()).isEqualTo(InvestmentType.STOCK);
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
        Investment original = Investment.create(
                UUID.randomUUID(), "Tesla Inc.", "TSLA",
                new BigDecimal("5"), new BigDecimal("200.00"),
                InvestmentType.STOCK, Instant.now().minusDays(10)
        );

        InvestmentEntity entity = mapper.toEntity(original);
        Investment restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getAssetName()).isEqualTo(original.getAssetName());
        assertThat(restored.getTicker()).isEqualTo(original.getTicker());
        assertThat(restored.getQuantity()).isEqualByComparingTo(original.getQuantity());
        assertThat(restored.getPurchasePrice()).isEqualByComparingTo(original.getPurchasePrice());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}