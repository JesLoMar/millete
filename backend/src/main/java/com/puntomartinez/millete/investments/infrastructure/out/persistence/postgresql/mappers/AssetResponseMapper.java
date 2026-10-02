package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.AssetApiDTOs;
import org.springframework.stereotype.Component;

@Component
public class AssetResponseMapper {

    public AssetApiDTOs.SharedAssetResponseDTO toResponse(
            SharedAsset asset
    ) {
        if (asset == null) {
            return null;
        }

        return new AssetApiDTOs.SharedAssetResponseDTO(
                asset.getId(),
                asset.getStableCatalogId(),
                asset.getName(),
                asset.getSymbol(),
                asset.getType(),
                sector(asset.getSector()),
                asset.getCurrency().value(),
                asset.getCreatedAt(),
                asset.getModifiedAt()
        );
    }

    public AssetApiDTOs.UserAssetResponseDTO toResponse(
            UserAsset asset
    ) {
        if (asset == null) {
            return null;
        }

        return new AssetApiDTOs.UserAssetResponseDTO(
                asset.getId(),
                asset.getUserId(),
                asset.getName(),
                asset.getType(),
                sector(asset.getSector()),
                asset.getOrigin(),
                asset.getCurrency().value(),
                asset.getCreatedAt(),
                asset.getModifiedAt()
        );
    }

    public AssetApiDTOs.UserAssetPriceResponseDTO toResponse(
            UserAssetPrice price
    ) {
        if (price == null) {
            return null;
        }

        return new AssetApiDTOs.UserAssetPriceResponseDTO(
                price.getId(),
                price.getUserAssetId(),
                price.getUnitPrice().amount(),
                price.getUnitPrice().currency().value(),
                price.getTimestamp()
        );
    }

    public AssetApiDTOs.SectorResponseDTO toResponse(
            AssetSector sector
    ) {
        return sector(sector);
    }

    private AssetApiDTOs.SectorResponseDTO sector(
            AssetSector sector
    ) {
        if (sector == null) {
            return null;
        }

        return new AssetApiDTOs.SectorResponseDTO(
                sector.code(),
                sector.displayName(),
                sector.custom()
        );
    }
}