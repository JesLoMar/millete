package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.HoldingStatus;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.HoldingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HoldingEntityMapper {

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
            target = "acquisitionCost",
            source = "acquisitionCost.amount"
    )
    @Mapping(
            target = "acquisitionCostCurrency",
            source = "acquisitionCost.currency.value"
    )
    @Mapping(
            target = "status",
            source = "status",
            qualifiedByName = "holdingStatusToString"
    )
    HoldingEntity toEntity(Holding domain);

    default Holding toDomain(HoldingEntity entity) {
        if (entity == null) {
            return null;
        }

        return Holding.reconstitute(
                entity.getId(),
                entity.getUserId(),
                readAssetReference(
                        entity.getAssetReferenceKind(),
                        entity.getAssetReferenceId()
                ),
                entity.getSnapshotAt(),
                entity.getQuantity(),
                new Money(
                        entity.getAcquisitionCost(),
                        CurrencyCode.of(
                                entity.getAcquisitionCostCurrency()
                        )
                ),
                HoldingStatus.valueOf(
                        entity.getStatus()
                ),
                entity.getCreatedAt(),
                entity.getSupersededAt()
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

    @Named("holdingStatusToString")
    default String holdingStatusToString(
            HoldingStatus status
    ) {
        return status != null
                ? status.name()
                : null;
    }

    default AssetReference readAssetReference(
            String kind,
            UUID id
    ) {
        if (kind == null || id == null) {
            throw new IllegalStateException(
                    "Un Holding persistido debe tener un AssetReference válido."
            );
        }

        return switch (AssetReferenceKind.valueOf(kind)) {
            case SHARED -> AssetReference.shared(id);
            case USER -> AssetReference.user(id);
        };
    }
}