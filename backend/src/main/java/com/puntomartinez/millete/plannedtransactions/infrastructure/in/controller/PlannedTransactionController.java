package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.ListPlannedTransactionsUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
import com.puntomartinez.millete.transactions.domain.model.Transaction.TransactionType;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase.RegisterPlannedTransactionCommand;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.UpdatePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.DeletePlannedTransactionUseCase;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.PlannedTransactionResponseDTO;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.RegisterPlannedTransactionRequestDTO;
import com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller.dto.UpdatePlannedTransactionRequestDTO;
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

    private final RegisterPlannedTransactionUseCase registerUseCase;
    private final ListPlannedTransactionsUseCase listPlannedTransactionsUseCase;
    private final UpdatePlannedTransactionUseCase updateUseCase;
    private final DeletePlannedTransactionUseCase deleteUseCase;
    private final CategoryRepository categoryRepository;

    public PlannedTransactionController(
            RegisterPlannedTransactionUseCase registerUseCase,
            ListPlannedTransactionsUseCase listPlannedTransactionsUseCase,
            UpdatePlannedTransactionUseCase updateUseCase,
            DeletePlannedTransactionUseCase deleteUseCase,
            CategoryRepository categoryRepository) {
        this.registerUseCase = registerUseCase;
        this.listPlannedTransactionsUseCase = listPlannedTransactionsUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ResponseEntity<PlannedTransactionResponseDTO> registerPlannedTransaction(
            @Valid @RequestBody RegisterPlannedTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        RegisterPlannedTransactionCommand command = new RegisterPlannedTransactionCommand(
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

        PlannedTransaction savedTransaction = registerUseCase.register(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedTransaction, userId));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<PlannedTransactionResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        TransactionType transactionType = parseType(type);

        long totalElements = listPlannedTransactionsUseCase.countByUserIdAndFilters(userId, search, transactionType);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int safePage = Math.min(page, Math.max(0, totalPages - 1));

        List<PlannedTransaction> list = listPlannedTransactionsUseCase.findAllByUserId(
                userId, safePage, size, search, transactionType);

        Map<UUID, CategoryInfo> categoryMap = categoryRepository.findByIdUsuario(userId).stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        c -> new CategoryInfo(c.getName()),
                        (a, b) -> a));

        List<PlannedTransactionResponseDTO> content = list.stream()
                .map(tx -> mapToResponse(tx, categoryMap))
                .toList();

        return ResponseEntity.ok(new PaginatedResponseDTO<>(
                content,
                safePage,
                totalPages,
                totalElements,
                size,
                safePage == 0,
                safePage >= totalPages - 1 || totalPages == 0
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlannedTransactionResponseDTO> updatePlannedTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlannedTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        var command = new UpdatePlannedTransactionUseCase.UpdatePlannedTransactionCommand(
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

        PlannedTransaction updated = updateUseCase.update(id, command);
        return ResponseEntity.ok(mapToResponse(updated, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlannedTransaction(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        deleteUseCase.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }

    private record CategoryInfo(String name) {}

    private CategoryInfo resolveCategoryInfo(UUID categoryId, UUID userId) {
        if (categoryId == null) {
            return new CategoryInfo("Sin categoría");
        }
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .map(c -> new CategoryInfo(c.getName()))
                .orElse(new CategoryInfo("Sin categoría"));
    }

    private PlannedTransactionResponseDTO mapToResponse(PlannedTransaction tx, UUID userId) {
        return mapToResponse(tx, resolveCategoryInfo(tx.getCategoryId(), userId));
    }

    private PlannedTransactionResponseDTO mapToResponse(PlannedTransaction tx, Map<UUID, CategoryInfo> categoryMap) {
        CategoryInfo info = tx.getCategoryId() != null ? categoryMap.get(tx.getCategoryId()) : null;
        if (info == null) {
            info = new CategoryInfo("Sin categoría");
        }
        return mapToResponse(tx, info);
    }

    private PlannedTransactionResponseDTO mapToResponse(PlannedTransaction tx, CategoryInfo info) {
        return new PlannedTransactionResponseDTO(
                tx.getId(),
                tx.getCategoryId(),
                info.name(),
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

    private TransactionType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
