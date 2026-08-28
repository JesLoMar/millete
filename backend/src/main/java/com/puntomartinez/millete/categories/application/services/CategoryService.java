package com.puntomartinez.millete.categories.application.services;

import com.puntomartinez.millete.categories.domain.model.Category;
import com.puntomartinez.millete.categories.domain.ports.in.GetCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.RegisterCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryCommand;
import com.puntomartinez.millete.categories.domain.ports.in.UpdateCategoryUseCase;
import com.puntomartinez.millete.categories.domain.ports.out.CategoryRepository;
import com.puntomartinez.millete.transactions.domain.model.Transaction;
import com.puntomartinez.millete.transactions.domain.ports.out.TransactionRepository;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService implements RegisterCategoryUseCase, UpdateCategoryUseCase, GetCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Category register(RegisterCategoryCommand command) {
        Category newCategory = new Category(
                command.userId(),
                command.nombre(),
                command.color(),
                command.budgetLimit()
        );
        return categoryRepository.save(newCategory);
    }

    @Override
    public List<Category> findByUserId(UUID userId) {
        return categoryRepository.findByIdUsuario(userId).stream()
                .filter(Category::isActive)
                .toList();
    }

    @Override
    public List<Category> findAllByUserId(UUID userId, int page, int size, String search) {
        return categoryRepository.findAllByUserId(userId, page, size, search);
    }

    @Override
    public long countByUserIdAndFilters(UUID userId, String search) {
        return categoryRepository.countByUserIdAndFilters(userId, search);
    }

    @Override
    public Category update(UUID id, UUID userId, UpdateCategoryCommand command) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        category.updateDetails(command.nombre(), command.color(), command.budgetLimit());
        return categoryRepository.save(category);
    }

    public Category findByIdAndUserId(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }

    public void delete(UUID id, UUID userId) {
        Category category = categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        category.deactivate();
        categoryRepository.save(category);

        List<Transaction> transactions = transactionRepository.findAllByCategoryId(id);
        transactions.stream()
                .filter(tx -> tx.getUserId().equals(userId))
                .forEach(tx -> {
                    tx.setCategoryId(null);
                    tx.setModifiedAt(LocalDateTime.now());
                    transactionRepository.save(tx);
                });
    }
}
