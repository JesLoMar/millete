package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.Position;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.PositionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PositionEntityMapper {

    default PositionEntity toEntity(Position domain) {
        if (domain == null) {
            return null;
        }

        PositionEntity entity =
                new PositionEntity();

        /*
         * Position no tiene identidad en dominio.
         * El identificador pertenece únicamente a persistencia.
         */
        entity.setId(UUID.randomUUID());

        entity.setUserId(
                domain.userId()
        );

        entity.setAssetReferenceKind(
                domain.assetReference()
                        .kind()
                        .name()
        );

        entity.setAssetReferenceId(
                domain.assetReference()
                        .id()
        );

        entity.setQuantity(
                domain.quantity()
        );

        entity.setAcquisitionCost(
                domain.acquisitionCost()
                        .amount()
        );

        entity.setCurrency(
                domain.acquisitionCost()
                        .currency()
                        .value()
        );

        entity.setHistoryIncomplete(
                domain.historyIncomplete()
        );

        entity.setEstimated(
                domain.estimated()
        );

        return entity;
    }

    default Position toDomain(PositionEntity entity) {
        if (entity == null) {
            return null;
        }

        AssetReference assetReference =
                switch (
                        AssetReferenceKind.valueOf(
                                entity.getAssetReferenceKind()
                        )
                ) {
                    case SHARED ->
                            AssetReference.shared(
                                    entity.getAssetReferenceId()
                            );

                    case USER ->
                            AssetReference.user(
                                    entity.getAssetReferenceId()
                            );
                };

        return new Position(
                entity.getUserId(),
                assetReference,
                entity.getQuantity(),
                new Money(
                        entity.getAcquisitionCost(),
                        CurrencyCode.of(
                                entity.getCurrency()
                        )
                ),
                entity.isHistoryIncomplete(),
                entity.isEstimated()
        );
    }
}