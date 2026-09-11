package com.puntomartinez.millete.categories.infrastructure.in.controller;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.DeleteCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.GetCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryUseCase;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.CategoryResponseDTO;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.RegisterCategoryRequestDTO;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.UpdateCategoryRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@Validated
public class CategoryController {

    private final RegisterCategoryUseCase registerCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    public CategoryController(
            RegisterCategoryUseCase registerCategoryUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            GetCategoryUseCase getCategoryUseCase,
            DeleteCategoryUseCase deleteCategoryUseCase
    ) {
        this.registerCategoryUseCase = registerCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.getCategoryUseCase = getCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody RegisterCategoryRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        RegisterCategoryCommand command = new RegisterCategoryCommand(
                userId,
                request.name(),
                request.color(),
                request.budgetLimit()
        );

        Category category = registerCategoryUseCase.register(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponse(category));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<CategoryResponseDTO>> getAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search
    ) {
        validatePagination(page, size);

        UUID userId = getAuthenticatedUserId(authentication);

        long totalElements =
                getCategoryUseCase.countByUserIdAndFilters(
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

        List<CategoryResponseDTO> content =
                getCategoryUseCase
                        .findAllByUserId(
                                userId,
                                safePage,
                                size,
                                search
                        )
                        .stream()
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

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                request.name(),
                request.color(),
                request.budgetLimit()
        );

        Category category = updateCategoryUseCase.update(
                id,
                userId,
                command
        );

        return ResponseEntity.ok(mapToResponse(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        deleteCategoryUseCase.deleteByIdAndUserId(
                id,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        return ((JwtUser) authentication.getPrincipal()).getId();
    }

    private CategoryResponseDTO mapToResponse(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getUserId(),
                category.getName(),
                category.getColor(),
                category.getBudgetLimit(),
                category.getCreatedAt(),
                category.isActive()
        );
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
}