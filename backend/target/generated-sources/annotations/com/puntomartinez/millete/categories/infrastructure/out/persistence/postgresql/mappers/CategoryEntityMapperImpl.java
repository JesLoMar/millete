package com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.infrastructure.out.persistence.postgresql.entity.CategoryEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-25T19:20:15+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class CategoryEntityMapperImpl implements CategoryEntityMapper {

    @Override
    public CategoryEntity toEntity(Category domain) {
        if ( domain == null ) {
            return null;
        }

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setId( domain.getId() );
        categoryEntity.setUserId( domain.getUserId() );
        categoryEntity.setName( domain.getName() );
        categoryEntity.setColor( domain.getColor() );
        categoryEntity.setBudgetLimit( domain.getBudgetLimit() );
        categoryEntity.setCreatedAt( domain.getCreatedAt() );
        categoryEntity.setModifiedAt( domain.getModifiedAt() );
        categoryEntity.setActive( domain.isActive() );

        return categoryEntity;
    }
}
