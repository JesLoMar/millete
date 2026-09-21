package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryEntityMapper")
class CategoryEntityMapperTest {

    private final CategoryEntityMapper mapper =
            Mappers.getMapper(CategoryEntityMapper.class);

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2024, 1, 2, 11, 30);

        Category domain = Category.reconstitute(
                id,
                userId,
                "Food",
                "#FF5733",
                new BigDecimal("123.45"),
                createdAt,
                modifiedAt,
                true
        );

        CategoryEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getName()).isEqualTo("Food");
        assertThat(entity.getColor()).isEqualTo("#FF5733");
        assertThat(entity.getBudgetLimit()).isEqualByComparingTo("123.45");
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

        CategoryEntity entity = new CategoryEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setName("Food");
        entity.setColor("#FF5733");
        entity.setBudgetLimit(new BigDecimal("123.45"));
        entity.setCreatedAt(createdAt);
        entity.setModifiedAt(modifiedAt);
        entity.setActive(true);

        Category domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getUserId()).isEqualTo(userId);
        assertThat(domain.getName()).isEqualTo("Food");
        assertThat(domain.getColor()).isEqualTo("#FF5733");
        assertThat(domain.getBudgetLimit()).isEqualByComparingTo("123.45");
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
        Category original = Category.create(
                UUID.randomUUID(),
                "Food",
                "#FF5733",
                new BigDecimal("99.99")
        );

        CategoryEntity entity = mapper.toEntity(original);
        Category restored = mapper.toDomain(entity);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getUserId()).isEqualTo(original.getUserId());
        assertThat(restored.getName()).isEqualTo(original.getName());
        assertThat(restored.getColor()).isEqualTo(original.getColor());
        assertThat(restored.getBudgetLimit()).isEqualByComparingTo(original.getBudgetLimit());
        assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(restored.getModifiedAt()).isEqualTo(original.getModifiedAt());
        assertThat(restored.isActive()).isEqualTo(original.isActive());
    }
}