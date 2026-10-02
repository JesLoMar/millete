package com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers;

import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.model.Position;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.PortfolioApiDTOs;
import org.springframework.stereotype.Component;

@Component
public class PortfolioResponseMapper {

    public PortfolioApiDTOs.HoldingResponseDTO toResponse(
            Holding holding
    ) {
        if (holding == null) {
            return null;
        }

        return new PortfolioApiDTOs.HoldingResponseDTO(
                holding.getId(),
                holding.getUserId(),
                assetReference(
                        holding.getAssetReference()
                ),
                holding.getSnapshotAt(),
                holding.getQuantity(),
                holding.getAcquisitionCost().amount(),
                holding.getAcquisitionCost()
                        .currency()
                        .value(),
                holding.getStatus().name(),
                holding.getCreatedAt(),
                holding.getSupersededAt()
        );
    }

    public PortfolioApiDTOs.PositionResponseDTO toResponse(
            com.puntomartinez.millete.investments.domain.ports.in.GetPortfolioAtUseCase.PositionResult position
    ) {
        if (position == null) {
            return null;
        }

        return new PortfolioApiDTOs.PositionResponseDTO(
                assetReference(
                        position.assetReference()
                ),
                position.quantity(),
                position.costBasis(),
                position.marketValue(),
                position.unrealizedGain(),
                position.valuationCurrency(),
                position.calculationStatus(),
                position.estimated(),
                position.historyIncomplete(),
                position.unavailableReason()
        );
    }

    public PortfolioApiDTOs.PortfolioResponseDTO toResponse(
            com.puntomartinez.millete.investments.domain.ports.in.GetPortfolioAtUseCase.PortfolioResult portfolio
    ) {
        if (portfolio == null) {
            return null;
        }

        return new PortfolioApiDTOs.PortfolioResponseDTO(
                portfolio.asOf(),
                portfolio.localCurrency(),
                portfolio.cashBalances(),
                portfolio.positions()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                portfolio.totalInLocalCurrency(),
                portfolio.estimated()
        );
    }

    public PortfolioApiDTOs.ClosedLotResponseDTO toResponse(
            com.puntomartinez.millete.investments.domain.ports.in.ListClosedLotsUseCase.ClosedLotResult lot
    ) {
        if (lot == null) {
            return null;
        }

        return new PortfolioApiDTOs.ClosedLotResponseDTO(
                lot.lotId(),
                assetReference(
                        lot.assetReference()
                ),
                lot.quantity(),
                lot.costBasis(),
                lot.costBasisCurrency(),
                lot.proceeds(),
                lot.proceedsCurrency(),
                lot.realizedGain(),
                lot.realizedGainCurrency(),
                lot.openedAt(),
                lot.closedAt(),
                lot.estimated(),
                lot.historyIncomplete()
        );
    }

    private PortfolioApiDTOs.AssetReferenceResponseDTO assetReference(
            AssetReference reference
    ) {
        if (reference == null) {
            return null;
        }

        return new PortfolioApiDTOs.AssetReferenceResponseDTO(
                reference.kind(),
                reference.id()
        );
    }
}