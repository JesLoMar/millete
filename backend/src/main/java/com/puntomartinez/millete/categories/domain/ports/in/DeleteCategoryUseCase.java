package com.puntomartinez.millete.categories.domain.ports.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {

    void deleteByIdAndUserId(UUID id, UUID userId);
}