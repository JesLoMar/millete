package com.puntomartinez.millete.plannedtransactions.domain.ports.out;

import java.util.UUID;

public interface CategoryExistencePort {

    boolean existsForUser(UUID categoryId, UUID userId);
}