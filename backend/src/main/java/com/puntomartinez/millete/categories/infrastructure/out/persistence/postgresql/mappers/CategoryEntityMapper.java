package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryEntityMapper {

    CategoryEntity toEntity(Category domain);

    default Category toDomain(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return Category.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getColor(),
                entity.getBudgetLimit(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }
}