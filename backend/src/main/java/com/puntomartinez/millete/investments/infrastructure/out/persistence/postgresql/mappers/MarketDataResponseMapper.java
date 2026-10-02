package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.MarketDataApiDTOs;
import org.springframework.stereotype.Component;

@Component
public class MarketDataResponseMapper {

    public MarketDataApiDTOs.AssetPriceResponseDTO toResponse(
            AssetPrice price
    ) {
        if (price == null) {
            return null;
        }

        return new MarketDataApiDTOs.AssetPriceResponseDTO(
                price.id(),
                price.sharedAssetId(),
                price.timestamp(),
                price.open(),
                price.high(),
                price.low(),
                price.close(),
                price.adjustedClose(),
                price.valuationPrice(),
                price.volume(),
                price.currency().value(),
                price.source(),
                price.fetchedAt()
        );
    }

    public MarketDataApiDTOs.FxRateResponseDTO toResponse(
            FxRate rate
    ) {
        if (rate == null) {
            return null;
        }

        return new MarketDataApiDTOs.FxRateResponseDTO(
                rate.id(),
                rate.baseCurrency().value(),
                rate.quoteCurrency().value(),
                rate.timestamp(),
                rate.rate(),
                rate.source(),
                rate.fetchedAt()
        );
    }
}