package com.puntomartinez.millete.savingsgoals.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import com.puntomartinez.millete.savingsgoals.domain.model.SavingsGoal;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.AddContributionToGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.AddContributionToGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.CreateSavingsGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.CreateSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.DeleteSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.GetSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.ListSavingsGoalsUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.UpdateSavingsGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.UpdateSavingsGoalUseCase;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.WithdrawFromGoalCommand;
import com.puntomartinez.millete.savingsgoals.domain.ports.in.WithdrawFromGoalUseCase;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.AddContributionRequestDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.CreateSavingsGoalRequestDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.SavingsGoalResponseDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.UpdateSavingsGoalRequestDTO;
import com.puntomartinez.millete.savingsgoals.infrastructure.in.controller.dto.WithdrawRequestDTO;
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

    CreateSavingsGoalCommand command = new CreateSavingsGoalCommand(
            userId,
            request.getName(),
            request.getTargetAmount(),
            request.getDeadline(),
            request.getPriority(),
            request.getLink()
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
    validatePagination(page, size);

    UUID userId = getUserId(authentication);

    long totalElements =
            listSavingsGoalsUseCase.countByUserIdAndFilters(
                    userId,
                    search
            );

    int totalPages = (int) Math.ceil(
            (double) totalElements / size
    );

    int safePage = Math.min(
            page,
            Math.max(0, totalPages - 1)
    );

    List<SavingsGoal> goals =
            listSavingsGoalsUseCase.findByUserId(
                    userId,
                    safePage,
                    size,
                    search
            );

    List<SavingsGoalResponseDTO> content =
            goals.stream()
                    .map(this::mapToResponse)
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

@PutMapping("/{id}")
public ResponseEntity<SavingsGoalResponseDTO> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateSavingsGoalRequestDTO request,
        Authentication authentication
) {
    UUID userId = getUserId(authentication);

    UpdateSavingsGoalCommand command =
            new UpdateSavingsGoalCommand(
                    id,
                    userId,
                    request.getName(),
                    request.getTargetAmount(),
                    request.getDeadline(),
                    request.getPriority(),
                    request.getLink()
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
            new AddContributionToGoalCommand(
                    id,
                    userId,
                    request.getAmount()
            );

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
            new WithdrawFromGoalCommand(
                    id,
                    userId,
                    request.getAmount()
            );

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

    deleteSavingsGoalUseCase.deleteByIdAndUserId(
            id,
            userId
    );

    return ResponseEntity.noContent().build();
}

private UUID getUserId(Authentication authentication) {
    return ((JwtUser) authentication.getPrincipal()).getId();
}

private void validatePagination(int page, int size) {
    if (page < 0) {
        throw new IllegalArgumentException(
                "Page must be greater than or equal to zero."
        );
    }

    if (size <= 0) {
        throw new IllegalArgumentException(
                "Size must be greater than zero."
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