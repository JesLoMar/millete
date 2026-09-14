package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CategoryImportPort {

    Map<UUID, UUID> importCategories(
            List<CategorySnapshot> categories,
            UUID userId
    );
}