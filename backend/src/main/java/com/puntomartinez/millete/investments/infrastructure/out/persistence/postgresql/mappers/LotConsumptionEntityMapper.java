package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.LotConsumption;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.LotConsumptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LotConsumptionEntityMapper {

    default LotConsumptionEntity toEntity(
            LotConsumption domain,
            UUID userId
    ) {
        if (domain == null) {
            return null;
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId es obligatorio para persistir un LotConsumption."
            );
        }

        LotConsumptionEntity entity =
                new LotConsumptionEntity();

        entity.setId(domain.id());
        entity.setUserId(userId);
        entity.setSellActivityId(
                domain.sellActivityId()
        );
        entity.setLotId(
                domain.lotId()
        );
        entity.setQuantity(
                domain.quantity()
        );
        entity.setCostBasis(
                domain.costBasis().amount()
        );
        entity.setCurrency(
                domain.costBasis()
                        .currency()
                        .value()
        );

        return entity;
    }

    default LotConsumption toDomain(
            LotConsumptionEntity entity
    ) {
        if (entity == null) {
            return null;
        }

        return new LotConsumption(
                entity.getId(),
                entity.getSellActivityId(),
                entity.getLotId(),
                entity.getQuantity(),
                new Money(
                        entity.getCostBasis(),
                        CurrencyCode.of(
                                entity.getCurrency()
                        )
                )
        );
    }
}