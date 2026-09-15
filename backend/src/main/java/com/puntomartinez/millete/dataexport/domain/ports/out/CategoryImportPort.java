package com.puntomartinez.millete.dataexport.domain.ports.out;

import com.puntomartinez.millete.dataexport.domain.model.CategoryImportResult;
import com.puntomartinez.millete.dataexport.domain.model.CategorySnapshot;

import java.util.List;
import java.util.UUID;

public interface CategoryImportPort {

    CategoryImportResult importCategories(
            List<CategorySnapshot> categories,
            UUID userId
    );
}