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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<List<TransactionResponseDTO>> listTransactions(Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        List<TransactionResponseDTO> transactions = listTransactionsUseCase.findAllByUserId(userId)
                .stream()
                .map(tx -> mapToDTO(tx, userId))
                .toList();

        return ResponseEntity.ok(transactions);
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
}
