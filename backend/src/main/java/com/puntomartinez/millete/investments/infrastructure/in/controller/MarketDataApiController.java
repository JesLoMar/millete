package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.AssetPrice;
import com.puntomartinez.millete.investments.domain.model.FxRate;
import com.puntomartinez.millete.investments.domain.ports.in.ListAssetPricesUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListFxRatesUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RefreshMarketDataUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.MarketDataApiDTOs;
import com.puntomartinez.millete.investments.infrastructure.out.marketdata.MarketDataProviderException;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.MarketDataResponseMapper;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.ErrorResponseDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class MarketDataApiController {

    private static final int MAX_PAGE_SIZE = 200;

    private final ListAssetPricesUseCase listAssetPrices;
    private final ListFxRatesUseCase listFxRates;
    private final RefreshMarketDataUseCase refreshMarketData;
    private final MarketDataResponseMapper responseMapper;

    public MarketDataApiController(
            ListAssetPricesUseCase listAssetPrices,
            ListFxRatesUseCase listFxRates,
            RefreshMarketDataUseCase refreshMarketData,
            MarketDataResponseMapper responseMapper
    ) {
        this.listAssetPrices = listAssetPrices;
        this.listFxRates = listFxRates;
        this.refreshMarketData = refreshMarketData;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/shared-assets/{sharedAssetId}/prices")
    public PaginatedResponseDTO<
            MarketDataApiDTOs.AssetPriceResponseDTO
            > prices(
            @PathVariable UUID sharedAssetId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        validatePage(page, size);
        validatePeriod(from, to);

        List<AssetPrice> prices =
                listAssetPrices.list(
                        sharedAssetId,
                        from,
                        to
                );

        List<MarketDataApiDTOs.AssetPriceResponseDTO> result =
                prices.stream()
                        .map(responseMapper::toResponse)
                        .toList();

        return page(
                result,
                page,
                size
        );
    }

    @GetMapping("/fx-rates")
    public PaginatedResponseDTO<
            MarketDataApiDTOs.FxRateResponseDTO
            > fxRates(
            @RequestParam String baseCurrency,
            @RequestParam String quoteCurrency,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        validatePage(page, size);
        validatePeriod(from, to);

        List<FxRate> rates =
                listFxRates.list(
                        baseCurrency,
                        quoteCurrency,
                        from,
                        to
                );

        List<MarketDataApiDTOs.FxRateResponseDTO> result =
                rates.stream()
                        .map(responseMapper::toResponse)
                        .toList();

        return page(
                result,
                page,
                size
        );
    }

    @PostMapping("/market-data/refresh")
    public ResponseEntity<Void> refresh(
            @Valid
            @RequestBody
            MarketDataApiDTOs.RefreshMarketDataRequestDTO request
    ) {
        refreshMarketData.refresh(
                new RefreshMarketDataUseCase.RefreshMarketDataCommand(
                        request.from(),
                        request.to(),
                        request.currencyPairs()
                )
        );

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MarketDataProviderException.class)
    public ResponseEntity<ErrorResponseDTO>
    marketDataProviderFailure(
            MarketDataProviderException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(
                        new ErrorResponseDTO(
                                LocalDateTime.now(),
                                HttpStatus.BAD_GATEWAY.value(),
                                HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                                exception.getMessage(),
                                "/api/v1/investments/market-data/refresh"
                        )
                );
    }

    private void validatePeriod(
            Instant from,
            Instant to
    ) {
        if (from == null || to == null) {
            throw new InvalidInputException(
                    "from y to son obligatorios."
            );
        }

        if (from.isAfter(to)) {
            throw new InvalidInputException(
                    "from no puede ser posterior a to."
            );
        }
    }

    private void validatePage(
            int page,
            int size
    ) {
        if (page < 0
                || size < 1
                || size > MAX_PAGE_SIZE) {
            throw new InvalidInputException(
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
                all.subList(start, end),
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