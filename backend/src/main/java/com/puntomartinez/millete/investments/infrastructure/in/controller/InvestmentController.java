package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.*;
import com.puntomartinez.millete.investments.domain.ports.in.InvestmentUseCases;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentController {
    private final InvestmentUseCases useCases;
    public InvestmentController(InvestmentUseCases useCases) { this.useCases = useCases; }

    @PostMapping("/assets")
    public ResponseEntity<AssetResponseDTO> createAsset(@Valid @RequestBody CreateAssetRequestDTO r, Authentication a) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asset(useCases.createAsset(user(a), assetCommand(r))));
    }
    @PutMapping("/assets/{id}")
    public AssetResponseDTO updateAsset(@PathVariable UUID id, @Valid @RequestBody CreateAssetRequestDTO r, Authentication a) {
        return asset(useCases.updateAsset(user(a), id, assetCommand(r)));
    }
    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Void> hideAsset(@PathVariable UUID id, Authentication a) { useCases.hideAsset(user(a),id); return ResponseEntity.noContent().build(); }
    @GetMapping("/assets")
    public List<AssetResponseDTO> assets(@RequestParam(defaultValue="false") boolean includeInactive, Authentication a) {
        return useCases.listAssets(user(a),includeInactive).stream().map(this::asset).toList();
    }
    @GetMapping("/sectors")
    public List<AssetSector> sectors() { return useCases.listSectors(); }

    @PostMapping("/activities")
    public ResponseEntity<ActivityResponseDTO> recordActivity(@Valid @RequestBody RecordActivityRequestDTO r, Authentication a) {
        Activity activity = useCases.recordActivity(user(a), new InvestmentUseCases.RecordActivityCommand(r.type(),r.occurredAt(),r.assetId(),r.quantity(),r.unitPrice(),r.amount(),r.currency(),r.secondaryAmount(),r.secondaryCurrency(),r.ratio(),r.exchangeRate(),r.comment()));
        return ResponseEntity.status(HttpStatus.CREATED).body(activity(activity));
    }
    @GetMapping("/activities")
    public List<ActivityResponseDTO> activities(@RequestParam(required=false) UUID assetId, Authentication a) {
        return useCases.listActivities(user(a),assetId).stream().map(this::activity).toList();
    }
    @PatchMapping("/activities/{id}")
    public ActivityResponseDTO editActivity(@PathVariable UUID id, @RequestBody EditActivityRequest r, Authentication a) {
        return activity(useCases.editActivity(user(a),id,new InvestmentUseCases.EditActivityCommand(r.occurredAt(),r.quantity(),r.unitPrice(),r.amount(),r.ratio(),r.comment(),r.reason())));
    }
    @PatchMapping("/activities/{id}/comment")
    public ActivityResponseDTO editTransferComment(@PathVariable UUID id, @RequestBody CommentRequest r, Authentication a) {
        return activity(useCases.editTransferComment(user(a),id,r.comment()));
    }
    @GetMapping("/activities/{id}/audit")
    public List<ActivityAudit> activityAudit(@PathVariable UUID id, Authentication a) { return useCases.listActivityAudit(user(a),id); }

    @PostMapping("/holdings")
    public ResponseEntity<Holding> createHolding(@Valid @RequestBody CreateHoldingRequestDTO r, Authentication a) {
        Holding holding = useCases.createHolding(user(a),new InvestmentUseCases.CreateHoldingCommand(r.assetId(),r.snapshotAt(),r.quantity(),r.acquisitionCost(),r.currency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(holding);
    }
    @PostMapping("/holdings/{id}/history")
    public List<ActivityResponseDTO> replaceHoldingWithHistory(@PathVariable UUID id,
            @Valid @RequestBody ReplaceHoldingHistoryRequestDTO r, Authentication a) {
        List<InvestmentUseCases.RecordActivityCommand> commands = r.activities().stream()
                .map(this::activityCommand).toList();
        return useCases.replaceHoldingWithHistory(user(a), id, commands).stream()
                .map(this::activity).toList();
    }
    @GetMapping("/holdings")
    public List<Holding> holdings(@RequestParam(required=false) UUID assetId, Authentication a) { return useCases.listHoldings(user(a),assetId); }
    @GetMapping("/cash")
    public Map<String,BigDecimal> cash(Authentication a) { return useCases.cashBalances(user(a)); }
    @GetMapping("/portfolio")
    public InvestmentUseCases.PortfolioView portfolio(@RequestParam(required=false) Instant at, Authentication a) { return useCases.portfolio(user(a),at); }

    @PostMapping("/prices")
    public ResponseEntity<AssetPrice> addPrice(@Valid @RequestBody AddAssetPriceRequestDTO r, Authentication a) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCases.addPrice(user(a),new InvestmentUseCases.AddPriceCommand(r.assetId(),r.timestamp(),r.open(),r.high(),r.low(),r.close(),r.adjustedClose(),r.volume(),r.currency(),r.source())));
    }
    @GetMapping("/assets/{assetId}/prices")
    public List<AssetPrice> prices(@PathVariable UUID assetId, @RequestParam Instant from, @RequestParam Instant to, Authentication a) { return useCases.listPrices(user(a),assetId,from,to); }
    @PostMapping("/fx-rates")
    public ResponseEntity<FxRate> addFxRate(@Valid @RequestBody AddFxRateRequestDTO r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCases.addFxRate(new InvestmentUseCases.AddFxRateCommand(r.baseCurrency(),r.quoteCurrency(),r.timestamp(),r.rate(),r.source())));
    }
    @PostMapping("/market-data/refresh")
    public InvestmentUseCases.RefreshResult refresh(@RequestParam Instant from, @RequestParam Instant to, Authentication a) { return useCases.refreshFromProvider(user(a),from,to); }
    @GetMapping("/health")
    public InvestmentUseCases.HealthReport health(Authentication a) { return useCases.health(user(a)); }

    public record EditActivityRequest(Instant occurredAt, BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount, BigDecimal ratio, String comment, String reason) { }
    public record CommentRequest(String comment) { }
    private UUID user(Authentication a) { return ((JwtUser)a.getPrincipal()).getId(); }
    private InvestmentUseCases.CreateAssetCommand assetCommand(CreateAssetRequestDTO r) { return new InvestmentUseCases.CreateAssetCommand(r.name(),r.symbol(),r.type(),r.sectorId(),r.currency()); }
    private InvestmentUseCases.RecordActivityCommand activityCommand(RecordActivityRequestDTO r) { return new InvestmentUseCases.RecordActivityCommand(r.type(),r.occurredAt(),r.assetId(),r.quantity(),r.unitPrice(),r.amount(),r.currency(),r.secondaryAmount(),r.secondaryCurrency(),r.ratio(),r.exchangeRate(),r.comment()); }
    private AssetResponseDTO asset(Asset a) { return new AssetResponseDTO(a.getId(),a.getName(),a.getSymbol(),a.getType(),a.getSectorId(),a.getCurrency(),a.isActive(),a.getCreatedAt(),a.getModifiedAt()); }
    private ActivityResponseDTO activity(Activity a) { return new ActivityResponseDTO(a.getId(),a.getType(),a.getAssetId(),a.getOccurredAt(),a.getOrderingKey(),a.getQuantity(),a.getUnitPrice(),a.getAmount(),a.getCurrency(),a.getSecondaryAmount(),a.getSecondaryCurrency(),a.getRatio(),a.getLocalCurrency(),a.getFxRateToLocal(),a.getFxRateSource(),a.getFxRateTimestamp(),a.getAmountInLocal(),a.getComment(),a.getLinkedTransactionId()); }
}
