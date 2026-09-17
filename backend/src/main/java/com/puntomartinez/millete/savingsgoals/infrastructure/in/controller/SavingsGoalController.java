package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller;

import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.*;
import com.puntomartinez.millete.savingsgoals.domain.utils.GoalPriority;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
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
@RequestMapping("/api/v1/savings-goals")
public class SavingsGoalController {

    private static final int MAX_PAGE_SIZE = 200;

    private final CreateSavingsGoalUseCase createSavingsGoalUseCase;
    private final UpdateSavingsGoalUseCase updateSavingsGoalUseCase;
    private final AddContributionToGoalUseCase addContributionToGoalUseCase;
    private final WithdrawFromGoalUseCase withdrawFromGoalUseCase;
    private final ListSavingsGoalsUseCase listSavingsGoalsUseCase;
    private final GetSavingsGoalUseCase getSavingsGoalUseCase;
    private final DeleteSavingsGoalUseCase deleteSavingsGoalUseCase;

    public SavingsGoalController(
            CreateSavingsGoalUseCase createSavingsGoalUseCase,
            UpdateSavingsGoalUseCase updateSavingsGoalUseCase,
            AddContributionToGoalUseCase addContributionToGoalUseCase,
            WithdrawFromGoalUseCase withdrawFromGoalUseCase,
            ListSavingsGoalsUseCase listSavingsGoalsUseCase,
            GetSavingsGoalUseCase getSavingsGoalUseCase,
            DeleteSavingsGoalUseCase deleteSavingsGoalUseCase
    ) {
        this.createSavingsGoalUseCase = createSavingsGoalUseCase;
        this.updateSavingsGoalUseCase = updateSavingsGoalUseCase;
        this.addContributionToGoalUseCase = addContributionToGoalUseCase;
        this.withdrawFromGoalUseCase = withdrawFromGoalUseCase;
        this.listSavingsGoalsUseCase = listSavingsGoalsUseCase;
        this.getSavingsGoalUseCase = getSavingsGoalUseCase;
        this.deleteSavingsGoalUseCase = deleteSavingsGoalUseCase;
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponseDTO> create(
            @Valid @RequestBody CreateSavingsGoalRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        GoalPriority priority = request.priority() != null
                ? request.priority()
                : GoalPriority.MEDIUM;

        CreateSavingsGoalCommand command = new CreateSavingsGoalCommand(
                userId,
                request.name(),
                request.targetAmount(),
                request.deadline(),
                priority,
                request.link()
        );

        SavingsGoal goal = createSavingsGoalUseCase.create(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(goal));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<SavingsGoalResponseDTO>> getAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "90") int size,
            @RequestParam(required = false) String search
    ) {
        validatePaginationParams(page, size);
        UUID userId = getUserId(authentication);

        long totalElements =
                listSavingsGoalsUseCase.countByUserIdAndFilters(userId, search);

        int totalPages = (int) Math.ceil((double) totalElements / size);

        int maxValidPage = Math.max(0, totalPages - 1);
        if (page > maxValidPage) {
            throw new InvalidInputException(
                    "La página solicitada (" + page
                            + ") está fuera de rango. Página máxima disponible: "
                            + maxValidPage + "."
            );
        }

        List<SavingsGoal> goals =
                listSavingsGoalsUseCase.findByUserId(userId, page, size, search);

        List<SavingsGoalResponseDTO> content =
                goals.stream().map(this::mapToResponse).toList();

        return ResponseEntity.ok(
                new PaginatedResponseDTO<>(
                        content, page, totalPages, totalElements, size,
                        page == 0,
                        page >= totalPages - 1 || totalPages == 0
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponseDTO> getById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        SavingsGoal goal =
                getSavingsGoalUseCase.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(mapToResponse(goal));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SavingsGoalResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSavingsGoalRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        UpdateSavingsGoalCommand command = new UpdateSavingsGoalCommand(
                id,
                userId,
                request.name(),
                request.targetAmount(),
                request.deadline(),
                request.priority(),
                request.link()
        );

        SavingsGoal goal = updateSavingsGoalUseCase.update(command);
        return ResponseEntity.ok(mapToResponse(goal));
    }

    @PatchMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoalResponseDTO> addContribution(
            @PathVariable UUID id,
            @Valid @RequestBody AddContributionRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        AddContributionToGoalCommand command =
                new AddContributionToGoalCommand(id, userId, request.amount());

        SavingsGoal goal =
                addContributionToGoalUseCase.addContribution(command);
        return ResponseEntity.ok(mapToResponse(goal));
    }

    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<SavingsGoalResponseDTO> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody WithdrawRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        WithdrawFromGoalCommand command =
                new WithdrawFromGoalCommand(id, userId, request.amount());

        SavingsGoal goal =
                withdrawFromGoalUseCase.withdraw(command);
        return ResponseEntity.ok(mapToResponse(goal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        deleteSavingsGoalUseCase.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return ((JwtUser) authentication.getPrincipal()).getId();
    }

    private void validatePaginationParams(int page, int size) {
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
        if (size > MAX_PAGE_SIZE) {
            throw new InvalidInputException(
                    "El tamaño de página no puede superar "
                            + MAX_PAGE_SIZE + "."
            );
        }
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
                goal.getLink(),
                goal.getCreatedAt(),
                goal.getModifiedAt(),
                goal.isActive()
        );
    }
}