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
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
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

    private static final int MAX_PAGE_SIZE = 200;

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
        validatePaginationParams(page, size);

        UUID userId = getAuthenticatedUserId(authentication);

        long totalElements =
                getCategoryUseCase.countByUserIdAndFilters(
                        userId,
                        search
                );

        int totalPages = (int) Math.ceil(
                (double) totalElements / size
        );

        int maxValidPage = Math.max(0, totalPages - 1);
        if (page > maxValidPage) {
            throw new InvalidInputException(
                    "La página solicitada (" + page
                            + ") está fuera de rango. Página máxima disponible: "
                            + maxValidPage + "."
            );
        }

        List<CategoryResponseDTO> content =
                getCategoryUseCase
                        .findAllByUserId(
                                userId,
                                page,
                                size,
                                search
                        )
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ResponseEntity.ok(
                new PaginatedResponseDTO<>(
                        content,
                        page,
                        totalPages,
                        totalElements,
                        size,
                        page == 0,
                        page >= totalPages - 1 || totalPages == 0
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
}