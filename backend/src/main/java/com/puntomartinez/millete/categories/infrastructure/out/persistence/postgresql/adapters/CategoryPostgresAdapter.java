package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.adapters;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.mappers.CategoryMapper;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.repository.JpaCategoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CategoryPostgresAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpaRepository;
    private final CategoryMapper mapper;

    public CategoryPostgresAdapter(JpaCategoryRepository jpaRepository, CategoryMapper mapper) {
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
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Category> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public List<Category> findByIdUsuario(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> findCategoriesWithBudgetByUserId(UUID userId) {
        return jpaRepository.findCategoriesWithBudgetByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}