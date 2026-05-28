package com.puntomartinez.millete.transactions.infrastructure.in.controller;

import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.in.*;
import com.puntomartinez.millete.transactions.domain.ports.in.RegisterTransactionUseCase.RegisterTransactionCommand;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.RegisterTransactionRequestDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionMetricsResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.TransactionResponseDTO;
import com.puntomartinez.millete.transactions.infrastructure.in.controller.dto.UpdateTransactionRequestDTO;
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

    // =======================================================
    // GET: MÉTRICAS DE TRANSACCIONES (NUEVO)
    // =======================================================
    @GetMapping("/metrics")
    public ResponseEntity<TransactionMetricsResponseDTO> getMetrics(
            @RequestParam(defaultValue = "month") String period,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        var command = new GetTransactionMetricsUseCase.MetricsCommand(userId, period);
        return ResponseEntity.ok(transactionMetricsUseCase.getMetrics(command));
    }

    // =======================================================
    // GET: LISTAR TODOS LOS MOVIMIENTOS
    // =======================================================
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> listTransactions(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        List<TransactionResponseDTO> transactions = listTransactionsUseCase.findAllByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    // =======================================================
    // POST: REGISTRAR UN NUEVO MOVIMIENTO
    // =======================================================
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> registerTransaction(
            @Valid @RequestBody RegisterTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        RegisterTransactionCommand command = new RegisterTransactionCommand(
                userId,
                request.categoryId(),
                request.amount(),
                request.date(),
                request.type(),
                request.description()
        );

        RegisterTransactionUseCase.RegisterTransactionResult result = registerTransactionUseCase.register(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(result.transaction()));
    }

    // =======================================================
    // DELETE: ANULAR UN MOVIMIENTO
    // =======================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        deleteTransactionUseCase.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }

    // =======================================================
    // GET: OBTENER UN MOVIMIENTO ESPECÍFICO POR ID
    // =======================================================
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Transaction tx = getTransactionUseCase.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(mapToDTO(tx));
    }

    // =======================================================
    // PUT: ACTUALIZAR UN MOVIMIENTO
    // =======================================================
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        UpdateTransactionUseCase.UpdateTransactionCommand command = new UpdateTransactionUseCase.UpdateTransactionCommand(
                userId,
                request.amount(),
                request.date(),
                request.type(),
                request.description(),
                request.categoryId()
        );

        Transaction updatedTransaction = updateTransactionUseCase.update(id, command);
        return ResponseEntity.ok(mapToDTO(updatedTransaction));
    }

    // =======================================================
    // MÉTODOS AUXILIARES
    // =======================================================
    private TransactionResponseDTO mapToDTO(Transaction tx) {
        return new TransactionResponseDTO(
                tx.getId(),
                tx.getCategoryId(),
                tx.getAmount(),
                tx.getDate(),
                tx.getType(),
                tx.getDescription(),
                false,
                tx.isActive()
        );
    }
}