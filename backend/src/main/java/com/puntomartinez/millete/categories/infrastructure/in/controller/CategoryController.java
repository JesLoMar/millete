package com.puntomartinez.millete.categories.infrastructure.in.controller;

import com.puntomartinez.millete.categories.application.services.CategoryService;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.GetCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryUseCase;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.CategoryResponseDTO;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.RegisterCategoryRequestDTO;
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
    private final CategoryService categoryService;

    public CategoryController(RegisterCategoryUseCase registerCategoryUseCase,
                              UpdateCategoryUseCase updateCategoryUseCase,
                              GetCategoryUseCase getCategoryUseCase,
                              CategoryService categoryService) {
        this.registerCategoryUseCase = registerCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.getCategoryUseCase = getCategoryUseCase;
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody RegisterCategoryRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        RegisterCategoryCommand command = new RegisterCategoryCommand(
                userId,
                request.name(),
                request.color(),
                request.budgetLimit()
        );

        Category category = registerCategoryUseCase.register(command);
        return new ResponseEntity<>(mapToResponse(category), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<CategoryResponseDTO>> getAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        long totalElements = getCategoryUseCase.countByUserIdAndFilters(userId, search);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int safePage = Math.min(page, Math.max(0, totalPages - 1));

        List<CategoryResponseDTO> content = getCategoryUseCase.findAllByUserId(userId, safePage, size, search)
                .stream()
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

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody RegisterCategoryRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                request.name(),
                request.color(),
                request.budgetLimit()
        );

        Category c = updateCategoryUseCase.update(id, userId, command);
        return ResponseEntity.ok(mapToResponse(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        categoryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponseDTO mapToResponse(Category c) {
        return new CategoryResponseDTO(
                c.getId(),
                c.getUserId(),
                c.getName(),
                c.getColor(),
                c.getBudgetLimit(),
                c.getCreatedAt(),
                c.isActive()
        );
    }
}
