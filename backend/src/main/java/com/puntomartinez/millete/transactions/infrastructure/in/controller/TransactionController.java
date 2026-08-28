package com.puntomartinez.millete.transactions.infrastructure.in.controller;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.*;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.RegisterTransactionRequestDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionMetricsResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.UpdateTransactionRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
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

    private final CategoryRepository categoryRepository;


    @GetMapping("/metrics")
    public ResponseEntity<TransactionMetricsResponseDTO> getMetrics(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        var command = new GetTransactionMetricsUseCase.MetricsCommand(userId, period);
        return ResponseEntity.ok(transactionMetricsUseCase.getMetrics(command));
    }


    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<TransactionResponseDTO>> listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        Transaction.TransactionType transactionType = parseType(type);
        LocalDateTime[] range = getDateRange(period);

        long totalElements = listTransactionsUseCase.countByUserIdAndFilters(
                userId, search, transactionType, range[0], range[1]);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int safePage = Math.min(page, Math.max(0, totalPages - 1));

        List<Transaction> transactions = listTransactionsUseCase.findAllByUserId(
                userId, safePage, size, search, transactionType, range[0], range[1]);

        Map<UUID, CategoryInfo> categoryMap = categoryRepository.findByIdUsuario(userId).stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        c -> new CategoryInfo(c.getName(), c.getColor()),
                        (a, b) -> a));

        List<TransactionResponseDTO> content = transactions.stream()
                .map(tx -> mapToDTO(tx, categoryMap))
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


    @PostMapping
    public ResponseEntity<TransactionResponseDTO> registerTransaction(
            @Valid @RequestBody RegisterTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        RegisterTransactionCommand command = new RegisterTransactionCommand(
                userId,
                request.categoryId(),
                request.amount(),
                request.date(),
                request.type(),
                request.description()
        );

        RegisterTransactionUseCase.RegisterTransactionResult result = registerTransactionUseCase.register(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(result.transaction(), userId));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id, Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        deleteTransactionUseCase.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable UUID id, Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        Transaction tx = getTransactionUseCase.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(mapToDTO(tx, userId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        UpdateTransactionUseCase.UpdateTransactionCommand command = new UpdateTransactionUseCase.UpdateTransactionCommand(
                userId,
                request.amount(),
                request.date(),
                request.type(),
                request.description(),
                request.categoryId()
        );

        Transaction updatedTransaction = updateTransactionUseCase.update(id, command);
        return ResponseEntity.ok(mapToDTO(updatedTransaction, userId));
    }


    private record CategoryInfo(String name, String color) {}

    private CategoryInfo resolveCategoryInfo(UUID categoryId, UUID userId) {
        if (categoryId == null) {
            return new CategoryInfo("Sin categoría", null);
        }

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .map(c -> new CategoryInfo(c.getName(), c.getColor()))
                .orElse(new CategoryInfo("Sin categoría", null));
    }

    private TransactionResponseDTO mapToDTO(Transaction tx, UUID userId) {
        CategoryInfo info = resolveCategoryInfo(tx.getCategoryId(), userId);
        return mapToDTO(tx, info);
    }

    private TransactionResponseDTO mapToDTO(Transaction tx, CategoryInfo info) {
        return new TransactionResponseDTO(
                tx.getId(),
                tx.getCategoryId(),
                info.name(),
                info.color(),
                tx.getAmount(),
                tx.getDate(),
                tx.getType(),
                tx.getDescription(),
                false,
                tx.isActive()
        );
    }

    private TransactionResponseDTO mapToDTO(Transaction tx, Map<UUID, CategoryInfo> categoryMap) {
        CategoryInfo info = tx.getCategoryId() != null ? categoryMap.get(tx.getCategoryId()) : null;
        if (info == null) {
            info = new CategoryInfo("Sin categoría", null);
        }
        return mapToDTO(tx, info);
    }

    private Transaction.TransactionType parseType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return Transaction.TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toLowerCase()) {
            case "week" -> new LocalDateTime[]{
                    now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0),
                    now.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59)
            };
            case "month" -> new LocalDateTime[]{
                    now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                    now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59)
            };
            case "year" -> new LocalDateTime[]{
                    now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0),
                    now.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59)
            };
            default -> new LocalDateTime[]{
                    now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                    now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59)
            };
        };
    }
}
