package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.Holding;
import com.puntomartinez.millete.investments.domain.ports.in.CreateHoldingUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetActiveHoldingUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentCashUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetPortfolioAtUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListClosedLotsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListHoldingsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ReplaceHoldingWithHistoryUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.ActivityApiDTOs;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.PortfolioApiDTOs;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.ActivityResponseMapper;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.PortfolioResponseMapper;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class PortfolioApiController {

    private static final int MAX_PAGE_SIZE = 200;

    private final CreateHoldingUseCase createHolding;
    private final GetActiveHoldingUseCase getActiveHolding;
    private final ListHoldingsUseCase listHoldings;
    private final ReplaceHoldingWithHistoryUseCase replaceHoldingWithHistory;

    private final GetInvestmentCashUseCase getInvestmentCash;
    private final GetPortfolioAtUseCase getPortfolioAt;
    private final ListClosedLotsUseCase listClosedLots;

    private final PortfolioResponseMapper responseMapper;
    private final ActivityResponseMapper activityResponseMapper;

    public PortfolioApiController(
            CreateHoldingUseCase createHolding,
            GetActiveHoldingUseCase getActiveHolding,
            ListHoldingsUseCase listHoldings,
            ReplaceHoldingWithHistoryUseCase replaceHoldingWithHistory,
            GetInvestmentCashUseCase getInvestmentCash,
            GetPortfolioAtUseCase getPortfolioAt,
            ListClosedLotsUseCase listClosedLots,
            PortfolioResponseMapper responseMapper,
            ActivityResponseMapper activityResponseMapper
    ) {
        this.createHolding = createHolding;
        this.getActiveHolding = getActiveHolding;
        this.listHoldings = listHoldings;
        this.replaceHoldingWithHistory = replaceHoldingWithHistory;
        this.getInvestmentCash = getInvestmentCash;
        this.getPortfolioAt = getPortfolioAt;
        this.listClosedLots = listClosedLots;
        this.responseMapper = responseMapper;
        this.activityResponseMapper = activityResponseMapper;
    }

    @PostMapping("/holdings")
    public ResponseEntity<PortfolioApiDTOs.HoldingResponseDTO> createHolding(
            @Valid
            @RequestBody
            PortfolioApiDTOs.CreateHoldingRequestDTO request,
            Authentication authentication
    ) {
        Holding holding =
                createHolding.create(
                        user(authentication),
                        new CreateHoldingUseCase.CreateHoldingCommand(
                                assetReference(
                                        request.assetReference()
                                ),
                                request.snapshotAt(),
                                request.quantity(),
                                request.acquisitionCost()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        responseMapper.toResponse(holding)
                );
    }

    @GetMapping("/holdings")
    public PaginatedResponseDTO<PortfolioApiDTOs.HoldingResponseDTO> holdings(
            @RequestParam(required = false)
            AssetReferenceKind assetKind,

            @RequestParam(required = false)
            UUID assetId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "50")
            int size,

            Authentication authentication
    ) {
        validatePage(page, size);

        AssetReference reference =
                queryAssetReference(
                        assetKind,
                        assetId
                );

        List<PortfolioApiDTOs.HoldingResponseDTO> result =
                listHoldings
                        .list(
                                user(authentication),
                                reference
                        )
                        .stream()
                        .map(responseMapper::toResponse)
                        .toList();

        return page(
                result,
                page,
                size
        );
    }

    @GetMapping("/holdings/active")
    public PortfolioApiDTOs.HoldingResponseDTO activeHolding(
            @RequestParam
            AssetReferenceKind assetKind,

            @RequestParam
            UUID assetId,

            Authentication authentication
    ) {
        Holding holding =
                getActiveHolding.get(
                        user(authentication),
                        new AssetReference(
                                assetKind,
                                assetId
                        )
                );

        return responseMapper.toResponse(
                holding
        );
    }

    @PostMapping("/holdings/{id}/history")
    public List<ActivityApiDTOs.ActivityResponseDTO> replaceHoldingWithHistory(
            @PathVariable UUID id,

            @Valid
            @RequestBody
            PortfolioApiDTOs.ReplaceHoldingHistoryRequestDTO request,

            Authentication authentication
    ) {
        List<ReplaceHoldingWithHistoryUseCase.HistoricalActivity>
                history =
                request.history()
                        .stream()
                        .map(this::historicalActivity)
                        .toList();

        List<Activity> activities =
                replaceHoldingWithHistory.replace(
                        user(authentication),
                        id,
                        new ReplaceHoldingWithHistoryUseCase.ReplaceHoldingWithHistoryCommand(
                                history
                        )
                );

        return activities
                .stream()
                .map(activityResponseMapper::toResponse)
                .toList();
    }

    @GetMapping("/cash")
    public PortfolioApiDTOs.CashBalancesResponseDTO cash(
            @RequestParam
            Instant asOf,
            Authentication authentication
    ) {
        return new PortfolioApiDTOs.CashBalancesResponseDTO(
                getInvestmentCash.getCash(
                        user(authentication),
                        asOf
                )
        );
    }

    @GetMapping("/portfolio")
    public PortfolioApiDTOs.PortfolioResponseDTO portfolio(
            @RequestParam
            Instant asOf,
            Authentication authentication
    ) {
        return responseMapper.toResponse(
                getPortfolioAt.get(
                        user(authentication),
                        asOf
                )
        );
    }

    @GetMapping("/lots/closed")
    public PaginatedResponseDTO<PortfolioApiDTOs.ClosedLotResponseDTO> closedLots(
            @RequestParam
            Instant from,

            @RequestParam
            Instant to,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "50")
            int size,

            Authentication authentication
    ) {
        validatePage(page, size);

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "from no puede ser posterior a to."
            );
        }

        List<PortfolioApiDTOs.ClosedLotResponseDTO> result =
                listClosedLots
                        .list(
                                user(authentication),
                                from,
                                to
                        )
                        .stream()
                        .map(responseMapper::toResponse)
                        .toList();

        return page(
                result,
                page,
                size
        );
    }

    private AssetReference assetReference(
            PortfolioApiDTOs.AssetReferenceRequestDTO request
    ) {
        if (request == null) {
            return null;
        }

        return new AssetReference(
                request.kind(),
                request.id()
        );
    }

    private AssetReference queryAssetReference(
            AssetReferenceKind kind,
            UUID id
    ) {
        if (kind == null && id == null) {
            return null;
        }

        if (kind == null || id == null) {
            throw new IllegalArgumentException(
                    "assetKind y assetId deben especificarse conjuntamente."
            );
        }

        return new AssetReference(
                kind,
                id
        );
    }

    private ReplaceHoldingWithHistoryUseCase.HistoricalActivity
    historicalActivity(
            PortfolioApiDTOs.HistoricalActivityDTO request
    ) {
        return new ReplaceHoldingWithHistoryUseCase.HistoricalActivity(
                request.occurredAt(),
                historicalDetails(request)
        );
    }

    private ReplaceHoldingWithHistoryUseCase.HistoricalActivityDetails
    historicalDetails(
            PortfolioApiDTOs.HistoricalActivityDTO request
    ) {
        return switch (request.type()) {

            case BUY ->
                    new ReplaceHoldingWithHistoryUseCase.BuyDetails(
                            request.quantity(),
                            request.unitPrice(),
                            request.settlementCurrency(),
                            request.comment()
                    );

            case SELL ->
                    new ReplaceHoldingWithHistoryUseCase.SellDetails(
                            request.quantity(),
                            request.unitPrice(),
                            request.settlementCurrency(),
                            request.comment()
                    );

            case CASH_DIVIDEND ->
                    new ReplaceHoldingWithHistoryUseCase.CashDividendDetails(
                            request.amount(),
                            request.currency(),
                            request.comment()
                    );

            case IN_KIND_DIVIDEND ->
                    new ReplaceHoldingWithHistoryUseCase.InKindDividendDetails(
                            request.quantity(),
                            request.comment()
                    );

            case SPLIT ->
                    new ReplaceHoldingWithHistoryUseCase.SplitDetails(
                            request.ratio(),
                            request.comment()
                    );
        };
    }

    private UUID user(
            Authentication authentication
    ) {
        return ((JwtUser) authentication.getPrincipal())
                .getId();
    }

    private void validatePage(
            int page,
            int size
    ) {
        if (page < 0
                || size < 1
                || size > MAX_PAGE_SIZE) {

            throw new IllegalArgumentException(
                    "page debe ser >= 0 y size entre 1 y 200."
            );
        }
    }

    private <T> PaginatedResponseDTO<T> page(
            List<T> all,
            int requestedPage,
            int size
    ) {
        int totalPages =
                all.isEmpty()
                        ? 0
                        : (int) Math.ceil(
                        (double) all.size() / size
                );

        int page =
                Math.min(
                        requestedPage,
                        Math.max(
                                0,
                                totalPages - 1
                        )
                );

        int start =
                Math.min(
                        page * size,
                        all.size()
                );

        int end =
                Math.min(
                        start + size,
                        all.size()
                );

        return new PaginatedResponseDTO<>(
                all.subList(
                        start,
                        end
                ),
                page,
                totalPages,
                all.size(),
                size,
                page == 0,
                totalPages == 0
                        || page >= totalPages - 1
        );
    }
}