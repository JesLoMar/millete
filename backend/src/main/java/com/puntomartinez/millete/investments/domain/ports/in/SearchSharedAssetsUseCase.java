package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.SharedAsset;

import java.util.List;

public interface SearchSharedAssetsUseCase {

    List<SharedAsset> search(String search);
}