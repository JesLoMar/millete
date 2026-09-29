package com.puntomartinez.millete.investments.domain.ports.in;

import com.puntomartinez.millete.investments.domain.model.UserAsset;

import java.util.List;
import java.util.UUID;

public interface ListUserAssetsUseCase {

    List<UserAsset> list(UUID userId, String search);
}