package com.puntomartinez.millete.categories.domain.ports.out;

import com.puntomartinez.millete.categories.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID idEtiqueta);
    List<Category> findAll();
    List<Category> findByIdUsuario(UUID idUsuario);
    List<Category> findCategoriesWithBudgetByUserId(UUID userId);
}