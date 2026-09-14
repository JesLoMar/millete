package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.Investment;
import com.puntomartinez.millete.investments.domain.ports.in.DeleteInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentDistributionUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentEvolutionUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentMetricsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListInvestmentsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterInvestmentUseCase.RegisterInvestmentCommand;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentPriceUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateInvestmentUseCase.UpdateInvestmentCommand;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentDistributionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentEvolutionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentMetricsResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.RegisterInvestmentRequestDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.UpdateInvestmentPriceRequestDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.UpdateInvestmentRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentController {

    private final RegisterInvestmentUseCase registerUseCase;
    private final ListInvestmentsUseCase listUseCase;
    private final GetInvestmentUseCase getUseCase;
    private final UpdateInvestmentPriceUseCase updatePriceUseCase;
    private final DeleteInvestmentUseCase deleteUseCase;
    private final UpdateInvestmentUseCase updateUseCase;
    private final GetInvestmentMetricsUseCase getInvestmentMetricsUseCase;
    private final GetInvestmentEvolutionUseCase getInvestmentEvolutionUseCase;
    private final GetInvestmentDistributionUseCase getInvestmentDistributionUseCase;

    public InvestmentController(
            RegisterInvestmentUseCase registerUseCase,
            ListInvestmentsUseCase listUseCase,
            GetInvestmentUseCase getUseCase,
            UpdateInvestmentPriceUseCase updatePriceUseCase,
            DeleteInvestmentUseCase deleteUseCase,
            UpdateInvestmentUseCase updateUseCase,
            GetInvestmentMetricsUseCase getInvestmentMetricsUseCase,
            GetInvestmentEvolutionUseCase getInvestmentEvolutionUseCase,
            GetInvestmentDistributionUseCase getInvestmentDistributionUseCase) {

        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
        this.getUseCase = getUseCase;
        this.updatePriceUseCase = updatePriceUseCase;
        this.deleteUseCase = deleteUseCase;
        this.updateUseCase = updateUseCase;
        this.getInvestmentMetricsUseCase = getInvestmentMetricsUseCase;
        this.getInvestmentEvolutionUseCase = getInvestmentEvolutionUseCase;
        this.getInvestmentDistributionUseCase = getInvestmentDistributionUseCase;
    }

    @PostMapping
    public ResponseEntity<InvestmentResponseDTO> registerInvestment(
            @Valid @RequestBody RegisterInvestmentRequestDTO request,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        RegisterInvestmentCommand command =
                new RegisterInvestmentCommand(
                        userId,
                        request.assetName(),
                        request.ticker(),
                        request.quantity(),
                        request.purchasePrice(),
                        request.type(),
                        request.purchaseDate()
                );

        Investment savedInvestment =
                registerUseCase.register(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToDTO(savedInvestment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> updateInvestment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvestmentRequestDTO request,
            Authentication authentication) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        UpdateInvestmentCommand command =
                new UpdateInvestmentCommand(
                        id,
                        userId,
                        request.assetName(),
                        request.ticker(),
                        request.quantity(),
                        request.purchasePrice(),
                        request.type(),
                        request.purchaseDate()
                );

        Investment updatedInvestment =
                updateUseCase.update(command);

        return ResponseEntity.ok(
                mapToDTO(updatedInvestment)
        );
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<InvestmentResponseDTO>> getAllInvestments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        Investment.InvestmentType investmentType =
                parseType(type);

        long totalElements =
                listUseCase.countByUserIdAndFilters(
                        userId,
                        search,
                        investmentType
                );

        int totalPages =
                (int) Math.ceil(
                        (double) totalElements / size
                );

        int safePage =
                Math.min(
                        page,
                        Math.max(0, totalPages - 1)
                );

        List<Investment> investments =
                listUseCase.findAllByUserId(
                        userId,
                        safePage,
                        size,
                        search,
                        investmentType
                );

        List<InvestmentResponseDTO> content =
                investments.stream()
                        .map(this::mapToDTO)
                        .toList();

        PaginatedResponseDTO<InvestmentResponseDTO> response =
                new PaginatedResponseDTO<>(
                        content,
                        safePage,
                        totalPages,
                        totalElements,
                        size,
                        safePage == 0,
                        safePage >= totalPages - 1
                                || totalPages == 0
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponseDTO> getInvestment(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        Investment investment =
                getUseCase.getById(id, userId);

        return ResponseEntity.ok(
                mapToDTO(investment)
        );
    }

    @GetMapping("/metrics")
    public ResponseEntity<InvestmentMetricsResponseDTO> getInvestmentMetrics(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                getInvestmentMetricsUseCase.getInvestmentMetrics(
                        userId,
                        period
                )
        );
    }

    @GetMapping("/evolution")
    public ResponseEntity<InvestmentEvolutionResponseDTO> getInvestmentEvolution(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                getInvestmentEvolutionUseCase.getInvestmentEvolution(
                        userId,
                        period
                )
        );
    }

    @GetMapping("/distribution")
    public ResponseEntity<InvestmentDistributionResponseDTO> getInvestmentDistribution(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        return ResponseEntity.ok(
                getInvestmentDistributionUseCase.getInvestmentDistribution(
                        userId,
                        period
                )
        );
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<InvestmentResponseDTO> updateInvestmentPrice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInvestmentPriceRequestDTO request,
            Authentication authentication) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        Investment updatedInvestment =
                updatePriceUseCase.updatePrice(
                        id,
                        userId,
                        request.newPrice()
                );

        return ResponseEntity.ok(
                mapToDTO(updatedInvestment)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestment(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId =
                getAuthenticatedUserId(authentication);

        deleteUseCase.delete(id, userId);

        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId(
            Authentication authentication) {

        return ((JwtUser) authentication.getPrincipal())
                .getId();
    }

    private Investment.InvestmentType parseType(
            String type) {

        if (type == null || type.isBlank()) {
            return null;
        }

        try {
            return Investment.InvestmentType.valueOf(
                    type.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private InvestmentResponseDTO mapToDTO(
            Investment investment) {

        return new InvestmentResponseDTO(
                investment.getId(),
                investment.getAssetName(),
                investment.getTicker(),
                investment.getQuantity(),
                investment.getPurchasePrice(),
                investment.getCurrentPrice(),
                investment.getInvestedCapital(),
                investment.getCurrentValue(),
                investment.getProfitOrLoss(),
                investment.getReturnOnInvestmentPercentage(),
                investment.getType(),
                investment.getPurchaseDate(),
                investment.isActive()
        );
    }
}