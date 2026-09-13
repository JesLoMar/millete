package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.InvestmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvestmentEntityMapper {

    InvestmentEntity toEntity(Investment domain);

    default Investment toDomain(InvestmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return Investment.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getAssetName(),
                entity.getTicker(),
                entity.getQuantity(),
                entity.getPurchasePrice(),
                entity.getCurrentPrice(),
                Investment.InvestmentType.valueOf(entity.getType()),
                entity.getPurchaseDate(),
                entity.getCreatedAt(),
                entity.getModifiedAt(),
                entity.isActive()
        );
    }
}