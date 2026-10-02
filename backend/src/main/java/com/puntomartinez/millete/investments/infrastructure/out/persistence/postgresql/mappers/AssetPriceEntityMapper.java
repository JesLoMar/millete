package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.CurrencyCode;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.entity.AssetPriceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssetPriceEntityMapper {

    @Mapping(
            target = "currency",
            source = "currency.value"
    )
    AssetPriceEntity toEntity(AssetPrice domain);

    default AssetPrice toDomain(AssetPriceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AssetPrice(
                entity.getId(),
                entity.getSharedAssetId(),
                entity.getTimestamp(),
                entity.getOpen(),
                entity.getHigh(),
                entity.getLow(),
                entity.getClose(),
                entity.getAdjustedClose(),
                entity.getVolume(),
                CurrencyCode.of(entity.getCurrency()),
                entity.getSource(),
                entity.getFetchedAt()
        );
    }
}