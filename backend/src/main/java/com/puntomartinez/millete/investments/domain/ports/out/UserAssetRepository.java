package com.puntomartinez.millete.investments.domain.ports.out;

import com.puntomartinez.millete.investments.domain.model.UserAsset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAssetRepository {

    UserAsset save(UserAsset userAsset);

    Optional<UserAsset> findByIdAndUserId(
            UUID userAssetId,
            UUID userId
    );

    List<UserAsset> findAllByUserId(
            UUID userId,
            String search
    );
}