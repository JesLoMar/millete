package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetOrigin;
import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.AssetType;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.UserAssetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserAssetEntityMapper {

    @Mapping(
            target = "type",
            source = "type",
            qualifiedByName = "assetTypeToString"
    )
    @Mapping(
            target = "origin",
            source = "origin",
            qualifiedByName = "assetOriginToString"
    )
    @Mapping(
            target = "sectorCode",
            source = "sector",
            qualifiedByName = "sectorCode"
    )
    @Mapping(
            target = "sectorDisplayName",
            source = "sector",
            qualifiedByName = "sectorDisplayName"
    )
    @Mapping(
            target = "sectorCustom",
            source = "sector",
            qualifiedByName = "sectorCustom"
    )
    @Mapping(
            target = "currency",
            source = "currency.value"
    )
    UserAssetEntity toEntity(UserAsset domain);

    default UserAsset toDomain(UserAssetEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserAsset.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                mapStringToAssetType(entity.getType()),
                mapSector(entity),
                mapStringToAssetOrigin(entity.getOrigin()),
                CurrencyCode.of(entity.getCurrency()),
                entity.getCreatedAt(),
                entity.getModifiedAt()
        );
    }

    @Named("assetTypeToString")
    default String assetTypeToString(AssetType type) {
        return type != null ? type.name() : null;
    }

    @Named("assetOriginToString")
    default String assetOriginToString(AssetOrigin origin) {
        return origin != null ? origin.name() : null;
    }

    @Named("sectorCode")
    default String sectorCode(AssetSector sector) {
        return sector != null ? sector.code() : null;
    }

    @Named("sectorDisplayName")
    default String sectorDisplayName(AssetSector sector) {
        return sector != null ? sector.displayName() : null;
    }

    @Named("sectorCustom")
    default boolean sectorCustom(AssetSector sector) {
        return sector != null && sector.custom();
    }

    default AssetType mapStringToAssetType(String type) {
        return type != null
                ? AssetType.valueOf(type)
                : null;
    }

    default AssetOrigin mapStringToAssetOrigin(String origin) {
        return origin != null
                ? AssetOrigin.valueOf(origin)
                : null;
    }

    default AssetSector mapSector(UserAssetEntity entity) {
        String code = entity.getSectorCode();
        String displayName = entity.getSectorDisplayName();

        if (code == null && displayName == null) {
            throw new IllegalStateException(
                    "Un UserAsset persistido debe tener un sector."
            );
        }

        if (entity.isSectorCustom()) {
            return AssetSector.custom(displayName);
        }

        return AssetSector.common(code, displayName);
    }
}