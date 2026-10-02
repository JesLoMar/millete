package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.Lot;
import com.puntomartinez.millete.investments.domain.model.LotSource;
import com.puntomartinez.millete.investments.domain.model.LotSourceType;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.LotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LotEntityMapper {

    @Mapping(
            target = "assetReferenceKind",
            source = "assetReference.kind",
            qualifiedByName = "assetReferenceKindToString"
    )
    @Mapping(
            target = "assetReferenceId",
            source = "assetReference.id"
    )
    @Mapping(
            target = "sourceType",
            source = "source.type",
            qualifiedByName = "lotSourceTypeToString"
    )
    @Mapping(
            target = "sourceId",
            source = "source.id"
    )
    @Mapping(
            target = "totalCost",
            source = "totalCost.amount"
    )
    @Mapping(
            target = "currency",
            source = "totalCost.currency.value"
    )
    LotEntity toEntity(Lot domain);

    default Lot toDomain(LotEntity entity) {
        if (entity == null) {
            return null;
        }

        return Lot.reconstitute(
                entity.getId(),
                entity.getUserId(),
                readAssetReference(
                        entity.getAssetReferenceKind(),
                        entity.getAssetReferenceId()
                ),
                readLotSource(
                        entity.getSourceType(),
                        entity.getSourceId()
                ),
                entity.getAcquiredAt(),
                entity.getAcquisitionOrder(),
                entity.getOriginalQuantity(),
                entity.getRemainingQuantity(),
                new Money(
                        entity.getTotalCost(),
                        CurrencyCode.of(
                                entity.getCurrency()
                        )
                )
        );
    }

    @Named("assetReferenceKindToString")
    default String assetReferenceKindToString(
            AssetReferenceKind kind
    ) {
        return kind != null
                ? kind.name()
                : null;
    }

    @Named("lotSourceTypeToString")
    default String lotSourceTypeToString(
            LotSourceType type
    ) {
        return type != null
                ? type.name()
                : null;
    }

    default AssetReference readAssetReference(
            String kind,
            UUID id
    ) {
        if (kind == null || id == null) {
            throw new IllegalStateException(
                    "Un Lot persistido debe tener un AssetReference válido."
            );
        }

        return switch (AssetReferenceKind.valueOf(kind)) {
            case SHARED -> AssetReference.shared(id);
            case USER -> AssetReference.user(id);
        };
    }

    default LotSource readLotSource(
            String type,
            UUID id
    ) {
        if (type == null || id == null) {
            throw new IllegalStateException(
                    "Un Lot persistido debe tener un origen válido."
            );
        }

        return switch (LotSourceType.valueOf(type)) {
            case ACTIVITY -> LotSource.activity(id);
            case HOLDING -> LotSource.holding(id);
        };
    }
}