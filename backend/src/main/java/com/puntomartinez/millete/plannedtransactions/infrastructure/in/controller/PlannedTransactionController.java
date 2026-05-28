package com.puntomartinez.millete.plannedtransactions.infrastructure.in.controller;

import com.puntomartinez.millete.plannedtransactions.application.services.PlannedTransactionService;
import com.puntomartinez.millete.plannedtransactions.domain.model.PlannedTransaction;
import com.puntomartinez.millete.plannedtransactions.domain.ports.in.RegisterPlannedTransactionUseCase;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planned-transactions")
public class PlannedTransactionController {

    private final RegisterPlannedTransactionUseCase registerUseCase;
    private final PlannedTransactionService plannedTransactionService;
    private final UpdatePlannedTransactionUseCase updateUseCase;
    private final DeletePlannedTransactionUseCase deleteUseCase;

    public PlannedTransactionController(
            RegisterPlannedTransactionUseCase registerUseCase,
            PlannedTransactionService plannedTransactionService,
            UpdatePlannedTransactionUseCase updateUseCase,
            DeletePlannedTransactionUseCase deleteUseCase) {
        this.registerUseCase = registerUseCase;
        this.plannedTransactionService = plannedTransactionService;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @PostMapping
    public ResponseEntity<PlannedTransactionResponseDTO> registerPlannedTransaction(
            @Valid @RequestBody RegisterPlannedTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

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

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(savedTransaction));
    }

    @GetMapping
    public ResponseEntity<List<PlannedTransactionResponseDTO>> getAll(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<PlannedTransaction> list = plannedTransactionService.findByUserId(userId);

        List<PlannedTransactionResponseDTO> response = list.stream()
                .map(tx -> new PlannedTransactionResponseDTO(
                        tx.getId(),
                        tx.getCategoryId(),
                        tx.getAmount(),
                        tx.getType(),
                        tx.getDescription(),
                        tx.getFrequencyType(),
                        tx.getFrequencyInterval(),
                        tx.getStartDate(),
                        tx.getEndDate(),
                        tx.isActive()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlannedTransactionResponseDTO> updatePlannedTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlannedTransactionRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

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
        return ResponseEntity.ok(mapToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlannedTransaction(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        deleteUseCase.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }

    private PlannedTransactionResponseDTO mapToResponse(PlannedTransaction tx) {
        return new PlannedTransactionResponseDTO(
                tx.getId(),
                tx.getCategoryId(),
                tx.getAmount(),
                tx.getType(),
                tx.getDescription(),
                tx.getFrequencyType(),
                tx.getFrequencyInterval(),
                tx.getStartDate(),
                tx.getEndDate(),
                tx.isActive()
        );
    }
}