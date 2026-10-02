package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.Activity;
import com.puntomartinez.millete.investments.domain.model.ActivityType;
import com.puntomartinez.millete.investments.domain.model.AssetReference;
import com.puntomartinez.millete.investments.domain.model.AssetReferenceKind;
import com.puntomartinez.millete.investments.domain.model.CashActivityDetails;
import com.puntomartinez.millete.investments.domain.model.DividendUnitsDetails;
import com.puntomartinez.millete.investments.domain.model.ExchangeActivityDetails;
import com.puntomartinez.millete.investments.domain.model.OpeningPositionDetails;
import com.puntomartinez.millete.investments.domain.model.SplitActivityDetails;
import com.puntomartinez.millete.investments.domain.model.TradeActivityDetails;
import com.puntomartinez.millete.investments.domain.ports.in.EditActivityUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListActivitiesUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordBuyUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordCashDividendUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordDepositUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordExchangeUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordInKindDividendUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordSellUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordSplitUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RecordWithdrawalUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.ActivityApiDTOs;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.ActivityResponseMapper;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class ActivityApiController {

    private static final int MAX_PAGE_SIZE = 200;

    private final RecordBuyUseCase recordBuy;
    private final RecordSellUseCase recordSell;
    private final RecordCashDividendUseCase recordCashDividend;
    private final RecordInKindDividendUseCase recordInKindDividend;
    private final RecordDepositUseCase recordDeposit;
    private final RecordWithdrawalUseCase recordWithdrawal;
    private final RecordExchangeUseCase recordExchange;
    private final RecordSplitUseCase recordSplit;
    private final EditActivityUseCase editActivity;
    private final ListActivitiesUseCase listActivities;
    private final ActivityResponseMapper responseMapper;

    public ActivityApiController(
            RecordBuyUseCase recordBuy,
            RecordSellUseCase recordSell,
            RecordCashDividendUseCase recordCashDividend,
            RecordInKindDividendUseCase recordInKindDividend,
            RecordDepositUseCase recordDeposit,
            RecordWithdrawalUseCase recordWithdrawal,
            RecordExchangeUseCase recordExchange,
            RecordSplitUseCase recordSplit,
            EditActivityUseCase editActivity,
            ListActivitiesUseCase listActivities,
            ActivityResponseMapper responseMapper
    ) {
        this.recordBuy = recordBuy;
        this.recordSell = recordSell;
        this.recordCashDividend = recordCashDividend;
        this.recordInKindDividend = recordInKindDividend;
        this.recordDeposit = recordDeposit;
        this.recordWithdrawal = recordWithdrawal;
        this.recordExchange = recordExchange;
        this.recordSplit = recordSplit;
        this.editActivity = editActivity;
        this.listActivities = listActivities;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/activities/buy")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> buy(
            @Valid @RequestBody ActivityApiDTOs.BuyActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordBuy.record(
                user(authentication),
                new RecordBuyUseCase.RecordBuyCommand(
                        request.occurredAt(),
                        assetReference(request.assetReference()),
                        request.quantity(),
                        request.unitPrice(),
                        request.settlementCurrency(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @PostMapping("/activities/sell")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> sell(
            @Valid @RequestBody ActivityApiDTOs.SellActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordSell.record(
                user(authentication),
                new RecordSellUseCase.RecordSellCommand(
                        request.occurredAt(),
                        assetReference(request.assetReference()),
                        request.quantity(),
                        request.unitPrice(),
                        request.settlementCurrency(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @PostMapping("/activities/dividends")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> cashDividend(
            @Valid @RequestBody ActivityApiDTOs.CashDividendActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordCashDividend.record(
                user(authentication),
                new RecordCashDividendUseCase.RecordCashDividendCommand(
                        request.occurredAt(),
                        assetReference(request.assetReference()),
                        request.amount(),
                        request.currency(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @PostMapping("/activities/dividends/in-kind")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> inKindDividend(
            @Valid @RequestBody ActivityApiDTOs.InKindDividendActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordInKindDividend.record(
                user(authentication),
                new RecordInKindDividendUseCase.RecordInKindDividendCommand(
                        request.occurredAt(),
                        assetReference(request.assetReference()),
                        request.quantity(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @PostMapping("/activities/deposits")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> deposit(
            @Valid @RequestBody ActivityApiDTOs.DepositActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordDeposit.record(
                user(authentication),
                new RecordDepositUseCase.RecordDepositCommand(
                        request.occurredAt(),
                        request.amount(),
                        request.currency(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @PostMapping("/activities/withdrawals")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> withdrawal(
            @Valid @RequestBody ActivityApiDTOs.WithdrawalActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordWithdrawal.record(
                user(authentication),
                new RecordWithdrawalUseCase.RecordWithdrawalCommand(
                        request.occurredAt(),
                        request.amount(),
                        request.currency(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @PostMapping("/activities/exchanges")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> exchange(
            @Valid @RequestBody ActivityApiDTOs.ExchangeActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordExchange.record(
                user(authentication),
                new RecordExchangeUseCase.RecordExchangeCommand(
                        request.occurredAt(),
                        request.amountOrigin(),
                        request.currencyOrigin(),
                        request.currencyDestination(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @PostMapping("/activities/splits")
    public ResponseEntity<ActivityApiDTOs.ActivityResponseDTO> split(
            @Valid @RequestBody ActivityApiDTOs.SplitActivityRequestDTO request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Activity activity = recordSplit.record(
                user(authentication),
                new RecordSplitUseCase.RecordSplitCommand(
                        request.occurredAt(),
                        assetReference(request.assetReference()),
                        request.ratio(),
                        request.comment()
                ),
                idempotencyKey
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMapper.toResponse(activity));
    }

    @GetMapping("/activities")
    public PaginatedResponseDTO<ActivityApiDTOs.ActivityResponseDTO> activities(
            @RequestParam(required = false) AssetReferenceKind assetKind,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication
    ) {
        validatePage(page, size);
        validateAssetReferenceQuery(assetKind, assetId);

        AssetReference reference =
                assetId == null
                        ? null
                        : new AssetReference(
                                assetKind,
                                assetId
                        );

        List<ActivityApiDTOs.ActivityResponseDTO> filtered =
                listActivities.list(
                                user(authentication),
                                reference,
                                from,
                                to
                        )
                        .stream()
                        .filter(activity ->
                                type == null
                                        || activity.getType() == type
                        )
                        .filter(activity ->
                                currency == null
                                        || containsCurrency(
                                        activity,
                                        currency
                                )
                        )
                        .map(responseMapper::toResponse)
                        .toList();

        return page(
                filtered,
                page,
                size
        );
    }

    @PatchMapping("/activities/{id}")
    public ActivityApiDTOs.ActivityResponseDTO edit(
            @org.springframework.web.bind.annotation.PathVariable UUID id,
            @Valid @RequestBody ActivityApiDTOs.EditActivityRequestDTO request,
            Authentication authentication
    ) {
        EditActivityUseCase.ActivityEditDetails details =
                editDetails(request.details());

        Activity activity =
                editActivity.edit(
                        user(authentication),
                        id,
                        new EditActivityUseCase.EditActivityCommand(
                                request.occurredAt(),
                                details,
                                request.reason()
                        )
                );

        return responseMapper.toResponse(activity);
    }

    private EditActivityUseCase.ActivityEditDetails editDetails(
            ActivityApiDTOs.EditActivityDetailsDTO request
    ) {
        return switch (request.type()) {

            case BUY_SELL ->
                    new EditActivityUseCase.BuySellDetails(
                            request.quantity(),
                            request.unitPrice(),
                            request.settlementCurrency(),
                            request.comment()
                    );

            case CASH_DIVIDEND ->
                    new EditActivityUseCase.CashDividendDetails(
                            request.amount(),
                            request.comment()
                    );

            case IN_KIND_DIVIDEND ->
                    new EditActivityUseCase.InKindDividendDetails(
                            request.quantity(),
                            request.comment()
                    );

            case EXCHANGE ->
                    new EditActivityUseCase.ExchangeDetails(
                            request.amountOrigin(),
                            request.comment()
                    );

            case SPLIT ->
                    new EditActivityUseCase.SplitDetails(
                            request.ratio(),
                            request.comment()
                    );
        };
    }

    private AssetReference assetReference(
            ActivityApiDTOs.AssetReferenceRequestDTO request
    ) {
        if (request == null) {
            return null;
        }

        return new AssetReference(
                request.kind(),
                request.id()
        );
    }

    private void validateAssetReferenceQuery(
            AssetReferenceKind kind,
            UUID id
    ) {
        if (id != null && kind == null) {
            throw new IllegalArgumentException(
                    "assetKind es obligatorio cuando se especifica assetId."
            );
        }

        if (kind != null && id == null) {
            throw new IllegalArgumentException(
                    "assetId es obligatorio cuando se especifica assetKind."
            );
        }
    }

    private boolean containsCurrency(
            Activity activity,
            String requestedCurrency
    ) {
        if (requestedCurrency == null
                || requestedCurrency.isBlank()) {
            return true;
        }

        String currency =
                requestedCurrency.trim();

        if (activity.getDetails()
                instanceof TradeActivityDetails trade) {

            return sameCurrency(
                    trade.unitPrice().currency().value(),
                    currency
            ) || sameCurrency(
                    trade.settlement().amount().currency().value(),
                    currency
            );
        }

        if (activity.getDetails()
                instanceof CashActivityDetails cash) {

            return sameCurrency(
                    cash.amount().currency().value(),
                    currency
            );
        }

        if (activity.getDetails()
                instanceof ExchangeActivityDetails exchange) {

            return sameCurrency(
                    exchange.origin().currency().value(),
                    currency
            ) || sameCurrency(
                    exchange.destination().currency().value(),
                    currency
            );
        }

        if (activity.getDetails()
                instanceof DividendUnitsDetails dividend) {

            return sameCurrency(
                    dividend.referenceUnitPrice()
                            .currency()
                            .value(),
                    currency
            );
        }

        if (activity.getDetails()
                instanceof OpeningPositionDetails opening) {

            return sameCurrency(
                    opening.acquisitionCost()
                            .currency()
                            .value(),
                    currency
            );
        }

        if (activity.getDetails()
                instanceof SplitActivityDetails) {

            return false;
        }

        return false;
    }

    private boolean sameCurrency(
            String actual,
            String requested
    ) {
        return actual.equalsIgnoreCase(
                requested
        );
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