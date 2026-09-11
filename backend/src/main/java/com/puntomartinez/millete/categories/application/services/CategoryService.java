package com.puntomartinez.millete.categories.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.DeleteCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.GetCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import com.puntomartinez.millete.transactions.domain.ports.in.UnassignCategoryFromTransactionsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService implements
        RegisterCategoryUseCase,
        UpdateCategoryUseCase,
        GetCategoryUseCase,
        DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final UnassignCategoryFromTransactionsUseCase unassignCategoryFromTransactionsUseCase;

    public CategoryService(
            CategoryRepository categoryRepository,
            UnassignCategoryFromTransactionsUseCase unassignCategoryFromTransactionsUseCase
    ) {
        this.categoryRepository = categoryRepository;
        this.unassignCategoryFromTransactionsUseCase =
                unassignCategoryFromTransactionsUseCase;
    }

    @Override
    public Category register(RegisterCategoryCommand command) {
        Category category = Category.create(
                command.userId(),
                command.name(),
                command.color(),
                command.budgetLimit()
        );

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> findByUserId(UUID userId) {
        return categoryRepository.findByUserId(userId);
    }

    @Override
    public Category findByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        return getCategory(id, userId);
    }

    @Override
    public List<Category> findAllByUserId(
            UUID userId,
            int page,
            int size,
            String search
    ) {
        return categoryRepository.findAllByUserId(
                userId,
                page,
                size,
                search
        );
    }

    @Override
    public long countByUserIdAndFilters(
            UUID userId,
            String search
    ) {
        return categoryRepository.countByUserIdAndFilters(
                userId,
                search
        );
    }

    @Override
    public Category update(
            UUID id,
            UUID userId,
            UpdateCategoryCommand command
    ) {
        Category category = getCategory(id, userId);

        category.updateDetails(
                command.name(),
                command.color(),
                command.budgetLimit()
        );

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(
            UUID id,
            UUID userId
    ) {
        Category category = getCategory(id, userId);

        category.deactivate();
        categoryRepository.save(category);

        unassignCategoryFromTransactionsUseCase.unassignCategory(
                id,
                userId
        );
    }

    private Category getCategory(
            UUID id,
            UUID userId
    ) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Categoría no encontrada"
                        )
                );
    }
}