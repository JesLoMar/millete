package com.puntomartinez.millete.plannedtransactions.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDisplayPort {

    Optional<CategoryDisplay> findByIdAndUserId(
            UUID categoryId,
            UUID userId
    );

    List<CategoryDisplay> findByUserId(UUID userId);

    record CategoryDisplay(
            UUID id,
            String name,
            String color
    ) {
    }
}