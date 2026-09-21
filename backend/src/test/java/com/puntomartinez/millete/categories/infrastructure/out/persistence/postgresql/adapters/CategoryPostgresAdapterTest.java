package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.mappers.CategoryEntityMapper;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.repository.JpaCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryPostgresAdapter")
class CategoryPostgresAdapterTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private JpaCategoryRepository jpaRepository;

    @Mock
    private CategoryEntityMapper mapper;

    @InjectMocks
    private CategoryPostgresAdapter adapter;

    private Category domainCategory() {
        return Category.create(
                USER_ID,
                "Food",
                "#FF5733",
                BigDecimal.TEN
        );
    }

    private CategoryEntity categoryEntity() {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(USER_ID);
        entity.setName("Food");
        entity.setColor("#FF5733");
        entity.setBudgetLimit(BigDecimal.TEN);
        entity.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        entity.setModifiedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        entity.setActive(true);
        return entity;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should map domain to entity, save it, and map saved entity back to domain")
        void shouldSaveCategory() {
            Category domain = domainCategory();
            CategoryEntity entity = categoryEntity();
            CategoryEntity savedEntity = categoryEntity();
            Category savedDomain = domainCategory();

            when(mapper.toEntity(domain)).thenReturn(entity);
            when(jpaRepository.save(entity)).thenReturn(savedEntity);
            when(mapper.toDomain(savedEntity)).thenReturn(savedDomain);

            Category result = adapter.save(domain);

            assertThat(result).isSameAs(savedDomain);
            verify(mapper).toEntity(domain);
            verify(jpaRepository).save(entity);
            verify(mapper).toDomain(savedEntity);
        }
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("Should return mapped domain when entity exists")
        void shouldReturnMappedDomainWhenEntityExists() {
            UUID categoryId = UUID.randomUUID();
            CategoryEntity entity = categoryEntity();
            Category domain = domainCategory();

            when(jpaRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<Category> result = adapter.findByIdAndUserId(categoryId, USER_ID);

            assertThat(result).contains(domain);
        }

        @Test
        @DisplayName("Should return empty when entity does not exist")
        void shouldReturnEmptyWhenEntityDoesNotExist() {
            UUID categoryId = UUID.randomUUID();

            when(jpaRepository.findByIdAndUserId(categoryId, USER_ID))
                    .thenReturn(Optional.empty());

            Optional<Category> result = adapter.findByIdAndUserId(categoryId, USER_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("list queries")
    class ListQueries {

        @Test
        @DisplayName("Should map findByUserId result to domain list")
        void shouldMapFindByUserIdResult() {
            CategoryEntity entity = categoryEntity();
            Category domain = domainCategory();

            when(jpaRepository.findByUserId(USER_ID)).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Category> result = adapter.findByUserId(USER_ID);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should map findByIdsAndUserId result to domain list")
        void shouldMapFindByIdsAndUserIdResult() {
            CategoryEntity entity = categoryEntity();
            Category domain = domainCategory();
            List<UUID> categoryIds = List.of(entity.getId());

            when(jpaRepository.findByUserIdAndIdIn(USER_ID, categoryIds))
                    .thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Category> result = adapter.findByIdsAndUserId(USER_ID, categoryIds);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should map findCategoriesWithBudgetByUserId result to domain list")
        void shouldMapFindCategoriesWithBudgetByUserIdResult() {
            CategoryEntity entity = categoryEntity();
            Category domain = domainCategory();

            when(jpaRepository.findCategoriesWithBudgetByUserId(USER_ID))
                    .thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Category> result = adapter.findCategoriesWithBudgetByUserId(USER_ID);

            assertThat(result).containsExactly(domain);
        }

        @Test
        @DisplayName("Should find all categories using pagination sorted by createdAt descending")
        void shouldFindAllCategoriesUsingPaginationAndSort() {
            CategoryEntity entity = categoryEntity();
            Category domain = domainCategory();

            Pageable expectedPageable = PageRequest.of(
                    0,
                    10,
                    Sort.by("createdAt").descending()
            );
            Page<CategoryEntity> page = new PageImpl<>(List.of(entity), expectedPageable, 1);

            when(jpaRepository.findAll(
                    ArgumentMatchers.<Specification<CategoryEntity>>any(),
                    any(Pageable.class)
            )).thenReturn(page);
            when(mapper.toDomain(entity)).thenReturn(domain);

            List<Category> result = adapter.findAllByUserId(USER_ID, 0, 10, "food");

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(jpaRepository).findAll(
                    ArgumentMatchers.<Specification<CategoryEntity>>any(),
                    pageableCaptor.capture()
            );

            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(result).containsExactly(domain);
            assertThat(capturedPageable.getPageNumber()).isZero();
            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
            assertThat(capturedPageable.getSort().getOrderFor("createdAt"))
                    .isNotNull();
            assertThat(capturedPageable.getSort().getOrderFor("createdAt").getDirection())
                    .isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    @DisplayName("count and exists queries")
    class CountAndExistsQueries {

        @Test
        @DisplayName("Should count categories using specification")
        void shouldCountCategoriesUsingSpecification() {
            when(jpaRepository.count(ArgumentMatchers.<Specification<CategoryEntity>>any()))
                    .thenReturn(3L);

            long result = adapter.countByUserIdAndFilters(USER_ID, "food");

            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should return true when active category name count is greater than zero")
        void shouldReturnTrueWhenActiveCategoryNameExists() {
            when(jpaRepository.countActiveByUserIdAndName(USER_ID, "Food"))
                    .thenReturn(1L);

            boolean result = adapter.existsActiveByUserIdAndName(USER_ID, "Food");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when active category name count is zero")
        void shouldReturnFalseWhenActiveCategoryNameDoesNotExist() {
            when(jpaRepository.countActiveByUserIdAndName(USER_ID, "Food"))
                    .thenReturn(0L);

            boolean result = adapter.existsActiveByUserIdAndName(USER_ID, "Food");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when active category name exists excluding id")
        void shouldReturnTrueWhenActiveCategoryNameExistsExcludingId() {
            UUID excludedId = UUID.randomUUID();

            when(jpaRepository.countActiveByUserIdAndNameExcludingId(USER_ID, "Food", excludedId))
                    .thenReturn(1L);

            boolean result =
                    adapter.existsActiveByUserIdAndNameExcludingId(USER_ID, "Food", excludedId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when active category name does not exist excluding id")
        void shouldReturnFalseWhenActiveCategoryNameDoesNotExistExcludingId() {
            UUID excludedId = UUID.randomUUID();

            when(jpaRepository.countActiveByUserIdAndNameExcludingId(USER_ID, "Food", excludedId))
                    .thenReturn(0L);

            boolean result =
                    adapter.existsActiveByUserIdAndNameExcludingId(USER_ID, "Food", excludedId);

            assertThat(result).isFalse();
        }
    }
}