package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.*;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/savings-goals")
public class SavingsGoalController {

    private final CreateSavingsGoalUseCase createSavingsGoalUseCase;
    private final UpdateSavingsGoalUseCase updateSavingsGoalUseCase;
    private final AddContributionToGoalUseCase addContributionToGoalUseCase;
    private final ListSavingsGoalsUseCase listSavingsGoalsUseCase;
    private final GetSavingsGoalUseCase getSavingsGoalUseCase;
    private final DeleteSavingsGoalUseCase deleteSavingsGoalUseCase;

    public SavingsGoalController(
            CreateSavingsGoalUseCase createSavingsGoalUseCase,
            UpdateSavingsGoalUseCase updateSavingsGoalUseCase,
            AddContributionToGoalUseCase addContributionToGoalUseCase,
            ListSavingsGoalsUseCase listSavingsGoalsUseCase,
            GetSavingsGoalUseCase getSavingsGoalUseCase,
            DeleteSavingsGoalUseCase deleteSavingsGoalUseCase) {
        this.createSavingsGoalUseCase = createSavingsGoalUseCase;
        this.updateSavingsGoalUseCase = updateSavingsGoalUseCase;
        this.addContributionToGoalUseCase = addContributionToGoalUseCase;
        this.listSavingsGoalsUseCase = listSavingsGoalsUseCase;
        this.getSavingsGoalUseCase = getSavingsGoalUseCase;
        this.deleteSavingsGoalUseCase = deleteSavingsGoalUseCase;
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponseDTO> create(
            @Valid @RequestBody CreateSavingsGoalRequestDTO request,
            Authentication authentication) {
        UUID userId = getUserId(authentication);

        CreateSavingsGoalCommand command = new CreateSavingsGoalCommand(
                userId,
                request.getName(),
                request.getTargetAmount(),
                request.getDeadline(),
                request.getPriority(),
                request.getLink()
        );

        SavingsGoal goal = createSavingsGoalUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(goal));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<SavingsGoalResponseDTO>> getAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "90") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        UUID userId = getUserId(authentication);
        String normalizedStatus = normalizeStatus(status);

        long totalElements = listSavingsGoalsUseCase.countByUserIdAndFilters(userId, search, normalizedStatus);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int safePage = Math.min(page, Math.max(0, totalPages - 1));

        List<SavingsGoal> goals = listSavingsGoalsUseCase.findByUserId(
                userId, safePage, size, search, normalizedStatus);

        List<SavingsGoalResponseDTO> content = goals.stream()
                .map(this::mapToResponse)
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

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.toUpperCase();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponseDTO> getById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        SavingsGoal goal = getSavingsGoalUseCase.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(mapToResponse(goal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSavingsGoalRequestDTO request,
            Authentication authentication) {
        UUID userId = getUserId(authentication);

        UpdateSavingsGoalCommand command = new UpdateSavingsGoalCommand(
                id,
                userId,
                request.getName(),
                request.getTargetAmount(),
                request.getDeadline(),
                request.getPriority(),
                request.getStatus(),
                request.getLink()
        );

        SavingsGoal goal = updateSavingsGoalUseCase.update(command);
        return ResponseEntity.ok(mapToResponse(goal));
    }

    @PatchMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoalResponseDTO> addContribution(
            @PathVariable UUID id,
            @Valid @RequestBody AddContributionRequestDTO request,
            Authentication authentication) {
        UUID userId = getUserId(authentication);

        AddContributionToGoalCommand command = new AddContributionToGoalCommand(
                id,
                userId,
                request.getAmount()
        );

        SavingsGoal goal = addContributionToGoalUseCase.addContribution(command);
        return ResponseEntity.ok(mapToResponse(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        deleteSavingsGoalUseCase.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return ((JwtUser) authentication.getPrincipal()).getId();
    }

    private SavingsGoalResponseDTO mapToResponse(SavingsGoal goal) {
        return new SavingsGoalResponseDTO(
                goal.getId(),
                goal.getUserId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getPriority(),
                goal.getStatus(),
                goal.getLink(),
                goal.getCreatedAt(),
                goal.getModifiedAt(),
                goal.isActive()
        );
    }
}
