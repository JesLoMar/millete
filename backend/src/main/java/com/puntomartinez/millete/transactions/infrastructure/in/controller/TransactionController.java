package com.puntomartinez.millete.transactions.infrastructure.in.controller;

import com.puntomartinez.millete.categories.domain.ports.in.GetCategoryUseCase;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import com.puntomartinez.millete.transactions.application.services.TransactionPeriodService;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.*;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.RegisterTransactionRequestDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionMetricsResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.UpdateTransactionRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final RegisterTransactionUseCase registerTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final GetTransactionMetricsUseCase transactionMetricsUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final TransactionPeriodService transactionPeriodService;

    @GetMapping("/metrics")
    public ResponseEntity<TransactionMetricsResponseDTO> getMetrics(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication
    ) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        GetTransactionMetricsUseCase.MetricsCommand command =
                new GetTransactionMetricsUseCase.MetricsCommand(
                        userId,
                        period
                );

        GetTransactionMetricsUseCase.MetricsResult result =
                transactionMetricsUseCase.getMetrics(command);

        TransactionMetricsResponseDTO response =
                new TransactionMetricsResponseDTO(
                        result.income(),
                        result.expenses(),
                        result.balance(),
                        result.count(),
                        result.incomeTrend(),
                        result.expensesTrend(),
                        result.balanceTrend(),
                        result.countTrend()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<TransactionResponseDTO>> listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication
    ) {
        validatePagination(page, size);

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        Transaction.TransactionType transactionType = parseType(type);

        LocalDate[] range =
                transactionPeriodService.getDateRange(period, userId);

        long totalElements =
                listTransactionsUseCase.countByUserIdAndFilters(
                        userId,
                        search,
                        transactionType,
                        range[0],
                        range[1]
                );

        int totalPages = (int) Math.ceil(
                (double) totalElements / size
        );

        int safePage = Math.min(
                page,
                Math.max(0, totalPages - 1)
        );

        List<Transaction> transactions =
                listTransactionsUseCase.findAllByUserId(
                        userId,
                        safePage,
                        size,
                        search,
                        transactionType,
                        range[0],
                        range[1]
                );

        Map<UUID, CategoryInfo> categoryMap =
                getCategoryUseCase.findByUserId(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                category -> category.getId(),
                                category -> new CategoryInfo(
                                        category.getName(),
                                        category.getColor()
                                ),
                                (a, b) -> a
                        ));

        List<TransactionResponseDTO> content =
                transactions.stream()
                        .map(tx -> mapToDTO(tx, categoryMap))
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

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> registerTransaction(
            @Valid @RequestBody RegisterTransactionRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        RegisterTransactionCommand command =
                new RegisterTransactionCommand(
                        userId,
                        request.categoryId(),
                        request.amount(),
                        request.date(),
                        request.type(),
                        request.description()
                );

        RegisterTransactionUseCase.RegisterTransactionResult result =
                registerTransactionUseCase.register(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToDTO(result.transaction(), userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        deleteTransactionUseCase.deleteByIdAndUserId(
                id,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        Transaction transaction =
                getTransactionUseCase.getByIdAndUserId(
                        id,
                        userId
                );

        return ResponseEntity.ok(
                mapToDTO(
                        transaction,
                        userId
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        UpdateTransactionUseCase.UpdateTransactionCommand command =
                new UpdateTransactionUseCase.UpdateTransactionCommand(
                        userId,
                        request.amount(),
                        request.date(),
                        request.type(),
                        request.description(),
                        request.categoryId()
                );

        Transaction updatedTransaction =
                updateTransactionUseCase.update(
                        id,
                        command
                );

        return ResponseEntity.ok(
                mapToDTO(
                        updatedTransaction,
                        userId
                )
        );
    }

    private record CategoryInfo(
            String name,
            String color
    ) {
    }

    private CategoryInfo resolveCategoryInfo(
            UUID categoryId,
            UUID userId
    ) {
        if (categoryId == null) {
            return new CategoryInfo(
                    "Sin categoría",
                    null
            );
        }

        try {
            var category =
                    getCategoryUseCase.findByIdAndUserId(
                            categoryId,
                            userId
                    );

            return new CategoryInfo(
                    category.getName(),
                    category.getColor()
            );
        } catch (ResourceNotFoundException e) {
            return new CategoryInfo(
                    "Sin categoría",
                    null
            );
        }
    }

    private TransactionResponseDTO mapToDTO(
            Transaction transaction,
            UUID userId
    ) {
        CategoryInfo info =
                resolveCategoryInfo(
                        transaction.getCategoryId(),
                        userId
                );

        return mapToDTO(
                transaction,
                info
        );
    }

    private TransactionResponseDTO mapToDTO(
            Transaction transaction,
            CategoryInfo info
    ) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getCategoryId(),
                info.name(),
                info.color(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getDescription(),
                false,
                transaction.isActive()
        );
    }

    private TransactionResponseDTO mapToDTO(
            Transaction transaction,
            Map<UUID, CategoryInfo> categoryMap
    ) {
        CategoryInfo info =
                transaction.getCategoryId() != null
                        ? categoryMap.get(transaction.getCategoryId())
                        : null;

        if (info == null) {
            info = new CategoryInfo(
                    "Sin categoría",
                    null
            );
        }

        return mapToDTO(
                transaction,
                info
        );
    }

    private Transaction.TransactionType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }

        try {
            return Transaction.TransactionType.valueOf(
                    type.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(
                    "Tipo de transacción no válido: " + type
            );
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidInputException(
                    "La página debe ser mayor o igual que 0."
            );
        }

        if (size <= 0) {
            throw new InvalidInputException(
                    "El tamaño de página debe ser mayor que 0."
            );
        }
    }
}
