package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.SharedAssetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SharedAssetEntityMapper {

    @Mapping(
            target = "type",
            source = "type",
            qualifiedByName = "assetTypeToString"
    )
    @Mapping(
            target = "sectorCode",
            source = "sector.code"
    )
    @Mapping(
            target = "sectorDisplayName",
            source = "sector.displayName"
    )
    @Mapping(
            target = "sectorCustom",
            source = "sector.custom"
    )
    @Mapping(
            target = "currency",
            source = "currency.value"
    )
    SharedAssetEntity toEntity(SharedAsset domain);

    default SharedAsset toDomain(SharedAssetEntity entity) {
        if (entity == null) {
            return null;
        }

        return SharedAsset.reconstitute(
                entity.getId(),
                entity.getStableCatalogId(),
                entity.getName(),
                entity.getSymbol(),
                AssetType.valueOf(entity.getType()),
                AssetSector.common(
                        entity.getSectorCode(),
                        entity.getSectorDisplayName()
                ),
                CurrencyCode.of(entity.getCurrency()),
                entity.getCreatedAt(),
                entity.getModifiedAt()
        );
    }

    @org.mapstruct.Named("assetTypeToString")
    default String assetTypeToString(AssetType type) {
        return type != null ? type.name() : null;
    }
}