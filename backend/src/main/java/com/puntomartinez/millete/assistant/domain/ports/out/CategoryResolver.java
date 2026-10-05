package com.puntomartinez.millete.assistant.domain.ports.out;

import com.puntomartinez.millete.assistant.domain.model.interpretation.CategoryResolution;

import java.util.UUID;

public interface CategoryResolver {

    CategoryResolution resolve(
            UUID userId,
            String categoryReference
    );
}