package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityDetails;
import com.puntomartinez.millete.investments.domain.model.AppliedFxRate;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.Money;
import com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.ActivityApiDTOs;
import org.springframework.stereotype.Component;

@Component
public class ActivityResponseMapper {

    public ActivityApiDTOs.ActivityResponseDTO toResponse(
            Activity activity
    ) {
        if (activity == null) {
            return null;
        }

        return new ActivityApiDTOs.ActivityResponseDTO(
                activity.getId(),
                activity.getType(),
                assetReference(activity.getAssetReference()),
                activity.getOccurredAt(),
                activity.getCreatedAt(),
                activity.getModifiedAt(),
                activity.getOrderingKey(),
                details(activity.getDetails()),
                activity.getComment(),
                activity.getLinkedTransactionId()
        );
    }

    private ActivityApiDTOs.AssetReferenceResponseDTO assetReference(
            AssetReference reference
    ) {
        if (reference == null) {
            return null;
        }

        return new ActivityApiDTOs.AssetReferenceResponseDTO(
                reference.kind(),
                reference.id()
        );
    }

    private ActivityApiDTOs.ActivityDetailsResponseDTO details(
            ActivityDetails details
    ) {
        if (details instanceof TradeActivityDetails trade) {
            return new ActivityApiDTOs.ActivityDetailsResponseDTO(
                    trade(trade),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        if (details instanceof CashActivityDetails cash) {
            return new ActivityApiDTOs.ActivityDetailsResponseDTO(
                    null,
                    new ActivityApiDTOs.CashActivityResponseDTO(
                            money(cash.amount())
                    ),
                    null,
                    null,
                    null,
                    null
            );
        }

        if (details instanceof SplitActivityDetails split) {
            return new ActivityApiDTOs.ActivityDetailsResponseDTO(
                    null,
                    null,
                    new ActivityApiDTOs.SplitActivityResponseDTO(
                            split.ratio()
                    ),
                    null,
                    null,
                    null
            );
        }

        if (details instanceof ExchangeActivityDetails exchange) {
            return new ActivityApiDTOs.ActivityDetailsResponseDTO(
                    null,
                    null,
                    null,
                    exchange(exchange),
                    null,
                    null
            );
        }

        if (details instanceof DividendUnitsDetails dividend) {
            return new ActivityApiDTOs.ActivityDetailsResponseDTO(
                    null,
                    null,
                    null,
                    null,
                    new ActivityApiDTOs.InKindDividendActivityResponseDTO(
                            dividend.quantity(),
                            money(dividend.referenceUnitPrice())
                    ),
                    null
            );
        }

        if (details instanceof OpeningPositionDetails openingPosition) {
            return new ActivityApiDTOs.ActivityDetailsResponseDTO(
                    null,
                    null,
                    null,
                    null,
                    null,
                    new ActivityApiDTOs.OpeningPositionActivityResponseDTO(
                            openingPosition.quantity(),
                            money(openingPosition.acquisitionCost())
                    )
            );
        }

        throw new IllegalStateException(
                "Tipo de ActivityDetails no soportado: "
                        + details.getClass().getName()
        );
    }

    private ActivityApiDTOs.TradeActivityResponseDTO trade(
            TradeActivityDetails trade
    ) {
        return new ActivityApiDTOs.TradeActivityResponseDTO(
                trade.quantity(),
                money(trade.unitPrice()),
                money(trade.settlement().amount()),
                fxRate(trade.settlement().appliedFxRate())
        );
    }

    private ActivityApiDTOs.ExchangeActivityResponseDTO exchange(
            ExchangeActivityDetails exchange
    ) {
        return new ActivityApiDTOs.ExchangeActivityResponseDTO(
                money(exchange.origin()),
                money(exchange.destination()),
                fxRate(exchange.appliedFxRate())
        );
    }

    private ActivityApiDTOs.MoneyResponseDTO money(
            Money money
    ) {
        if (money == null) {
            return null;
        }

        return new ActivityApiDTOs.MoneyResponseDTO(
                money.amount(),
                money.currency().value()
        );
    }

    private ActivityApiDTOs.AppliedFxRateResponseDTO fxRate(
            AppliedFxRate fxRate
    ) {
        if (fxRate == null) {
            return null;
        }

        return new ActivityApiDTOs.AppliedFxRateResponseDTO(
                fxRate.baseCurrency().value(),
                fxRate.quoteCurrency().value(),
                fxRate.rate(),
                fxRate.source(),
                fxRate.timestamp()
        );
    }
}