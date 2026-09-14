package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.mappers.CategoryEntityMapper;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.repository.JpaCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryPostgresAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpaRepository;
    private final CategoryEntityMapper mapper;

    public CategoryPostgresAdapter(
            JpaCategoryRepository jpaRepository,
            CategoryEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = mapper.toEntity(category);
        CategoryEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Category> findByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        return jpaRepository.findByIdAndUserId(id, userId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Category> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findByIdsAndUserId(
            UUID userId,
            List<UUID> categoryIds
    ) {
        return jpaRepository.findByUserIdAndIdIn(
                        userId,
                        categoryIds
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findCategoriesWithBudgetByUserId(UUID userId) {
        return jpaRepository.findCategoriesWithBudgetByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search
    ) {
        Specification<CategoryEntity> specification =
                buildSpecification(userId, search);

        Page<CategoryEntity> result = jpaRepository.findAll(
                specification,
                PageRequest.of(
                        page,
                        size,
                        Sort.by("createdAt").descending()
                )
        );

        return result.getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndFilters(
            UUID userId,
            String search
    ) {
        return jpaRepository.count(
                buildSpecification(userId, search)
        );
    }

    private Specification<CategoryEntity> buildSpecification(
            UUID userId,
            String search
    ) {
        Specification<CategoryEntity> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(
                                root.get("userId"),
                                userId
                        );

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get("name")),
                                    pattern
                            )
            );
        }

        return specification;
    }
}