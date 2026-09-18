package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller;

import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ListPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase.RegisterPlannedTransactionCommand;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.out.CategoryDisplayPort;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.PlannedTransactionResponseDTO;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.RegisterPlannedTransactionRequestDTO;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.UpdatePlannedTransactionRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/planned-transactions")
public class PlannedTransactionController {

    private static final String UNCATEGORIZED_LABEL = "Sin categoría";

    private final RegisterPlannedTransactionUseCase registerUseCase;
    private final ListPlannedTransactionsUseCase listPlannedTransactionsUseCase;
    private final UpdatePlannedTransactionUseCase updateUseCase;
    private final DeletePlannedTransactionUseCase deleteUseCase;
    private final CategoryDisplayPort categoryDisplayPort;

    public PlannedTransactionController(
            RegisterPlannedTransactionUseCase registerUseCase,
            ListPlannedTransactionsUseCase listPlannedTransactionsUseCase,
            UpdatePlannedTransactionUseCase updateUseCase,
            DeletePlannedTransactionUseCase deleteUseCase,
            CategoryDisplayPort categoryDisplayPort
    ) {
        this.registerUseCase = registerUseCase;
        this.listPlannedTransactionsUseCase = listPlannedTransactionsUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.categoryDisplayPort = categoryDisplayPort;
    }

    @PostMapping
    public ResponseEntity<PlannedTransactionResponseDTO> registerPlannedTransaction(
            @Valid @RequestBody RegisterPlannedTransactionRequestDTO request,
            Authentication authentication
    ) {
        UUID userId =
                ((JwtUser) authentication.getPrincipal()).getId();

        RegisterPlannedTransactionCommand command =
                new RegisterPlannedTransactionCommand(
                        userId,
                        request.categoryId(),
                        request.amount(),
                        request.type(),
                        request.description(),
                        request.frequencyType(),
                        request.frequencyInterval(),
                        request.startDate(),
                        request.endDate()
                );

        PlannedTransaction savedTransaction =
                registerUseCase.register(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(savedTransaction, userId));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<PlannedTransactionResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TransactionType type,
            Authentication authentication
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "La página debe ser mayor o igual que 0."
            );
        }
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe ser mayor que 0."
            );
        }

        UUID userId =
                ((JwtUser) authentication.getPrincipal()).getId();

        long totalElements =
                listPlannedTransactionsUseCase.countByUserIdAndFilters(
                        userId,
                        search,
                        type
                );

        int totalPages =
                (int) Math.ceil((double) totalElements / size);

        int safePage =
                Math.min(page, Math.max(0, totalPages - 1));

        List<PlannedTransaction> list =
                listPlannedTransactionsUseCase.findAllByUserId(
                        userId,
                        safePage,
                        size,
                        search,
                        type
                );

        Map<UUID, CategoryDisplayPort.CategoryDisplay> categoryMap =
                categoryDisplayPort.findByUserId(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                CategoryDisplayPort.CategoryDisplay::id,
                                display -> display,
                                (first, second) -> first
                        ));

        List<PlannedTransactionResponseDTO> content =
                list.stream()
                        .map(tx -> mapToResponse(tx, categoryMap))
                        .toList();

        return ResponseEntity.ok(
                new PaginatedResponseDTO<>(
                        content,
                        safePage,
                        totalPages,
                        totalElements,
                        size,
                        safePage == 0,
                        safePage >= totalPages - 1 || totalPages == 0
                )
        );
    }

    @PutMapping("/{id}")
public ResponseEntity<PlannedTransactionResponseDTO> updatePlannedTransaction(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePlannedTransactionRequestDTO request,
        Authentication authentication
) {
    UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

    UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand command =
            new UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand(
                    request.amount(),
                    request.type(),
                    request.description(),
                    request.frequencyType(),
                    request.frequencyInterval(),
                    request.categoryId()
            );

    PlannedTransaction updated =
            updateUseCase.update(id, userId, command);

    return ResponseEntity.ok(
            mapToResponse(updated, userId)
    );
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlannedTransaction(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId =
                ((JwtUser) authentication.getPrincipal()).getId();

        deleteUseCase.deleteByIdAndUserId(id, userId);

        return ResponseEntity.noContent().build();
    }

    private CategoryDisplayPort.CategoryDisplay resolveCategoryDisplay(
            UUID categoryId,
            UUID userId
    ) {
        if (categoryId == null) {
            return null;
        }

        return categoryDisplayPort
                .findByIdAndUserId(categoryId, userId)
                .orElse(null);
    }

    private PlannedTransactionResponseDTO mapToResponse(
            PlannedTransaction tx,
            UUID userId
    ) {
        CategoryDisplayPort.CategoryDisplay display =
                resolveCategoryDisplay(
                        tx.getCategoryId(),
                        userId
                );

        return mapToResponse(tx, display);
    }

    private PlannedTransactionResponseDTO mapToResponse(
            PlannedTransaction tx,
            Map<UUID, CategoryDisplayPort.CategoryDisplay> categoryMap
    ) {
        CategoryDisplayPort.CategoryDisplay display =
                tx.getCategoryId() != null
                        ? categoryMap.get(tx.getCategoryId())
                        : null;

        return mapToResponse(tx, display);
    }

    private PlannedTransactionResponseDTO mapToResponse(
            PlannedTransaction tx,
            CategoryDisplayPort.CategoryDisplay display
    ) {
        String categoryName = display != null
                ? display.name()
                : UNCATEGORIZED_LABEL;

        return new PlannedTransactionResponseDTO(
                tx.getId(),
                tx.getCategoryId(),
                categoryName,
                tx.getAmount(),
                tx.getType(),
                tx.getDescription(),
                tx.getFrequencyType(),
                tx.getFrequencyInterval(),
                tx.getStartDate(),
                tx.getEndDate(),
                tx.getLastExecutedDate(),
                tx.isActive()
        );
    }
}