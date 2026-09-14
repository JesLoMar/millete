package com.puntomartinez.millete.dashboard.domain.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CategoryQueryPort {

    List<CategoryData> findByUserId(UUID userId);

    List<CategoryData> findByIdsAndUserId(
            UUID userId,
            List<UUID> categoryIds
    );

    List<CategoryData> findCategoriesWithBudgetByUserId(UUID userId);

    record CategoryData(
            UUID id,
            String name,
            String color,
            BigDecimal budgetLimit
    ) {
    }
}