package com.puntomartinez.millete.categories.infrastructure.in.controller;

import com.puntomartinez.millete.categories.application.services.CategoryService;
import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryUseCase;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.CategoryResponseDTO;
import com.puntomartinez.millete.categories.infrastructure.in.controller.dto.RegisterCategoryRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final RegisterCategoryUseCase registerCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final CategoryService categoryService;

    public CategoryController(RegisterCategoryUseCase registerCategoryUseCase,
                              UpdateCategoryUseCase updateCategoryUseCase,
                              CategoryService categoryService) {
        this.registerCategoryUseCase = registerCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody RegisterCategoryRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        RegisterCategoryUseCase.RegisterCategoryCommand command = new RegisterCategoryUseCase.RegisterCategoryCommand(
                userId,
                request.name(),
                request.color(),
                request.budgetLimit()
        );
        Category category = registerCategoryUseCase.register(command);
        return new ResponseEntity<>(mapToResponse(category), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAll(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<CategoryResponseDTO> response = categoryService.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable UUID id, @RequestBody RegisterCategoryRequestDTO request) {
        UpdateCategoryUseCase.UpdateCategoryCommand command = new UpdateCategoryUseCase.UpdateCategoryCommand(
                request.name(),
                request.color(),
                request.budgetLimit()
        );
        Category c = updateCategoryUseCase.update(id, command);
        return ResponseEntity.ok(mapToResponse(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
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