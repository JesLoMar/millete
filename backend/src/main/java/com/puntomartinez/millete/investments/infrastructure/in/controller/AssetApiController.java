package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.AssetSector;
import com.puntomartinez.millete.investments.domain.model.SharedAsset;
import com.puntomartinez.millete.investments.domain.model.UserAsset;
import com.puntomartinez.millete.investments.domain.model.UserAssetPrice;
import com.puntomartinez.millete.investments.domain.ports.in.GetSharedAssetUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetUserAssetUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListAssetSectorsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.ListUserAssetsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterUserAssetPriceUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RegisterUserAssetUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.SearchSharedAssetsUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.UpdateUserAssetUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.AssetApiDTOs;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.AssetResponseMapper;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class AssetApiController {

    private final SearchSharedAssetsUseCase searchSharedAssets;
    private final GetSharedAssetUseCase getSharedAsset;
    private final ListAssetSectorsUseCase listAssetSectors;

    private final RegisterUserAssetUseCase registerUserAsset;
    private final UpdateUserAssetUseCase updateUserAsset;
    private final GetUserAssetUseCase getUserAsset;
    private final ListUserAssetsUseCase listUserAssets;
    private final RegisterUserAssetPriceUseCase registerUserAssetPrice;

    private final AssetResponseMapper responseMapper;

    public AssetApiController(
            SearchSharedAssetsUseCase searchSharedAssets,
            GetSharedAssetUseCase getSharedAsset,
            ListAssetSectorsUseCase listAssetSectors,
            RegisterUserAssetUseCase registerUserAsset,
            UpdateUserAssetUseCase updateUserAsset,
            GetUserAssetUseCase getUserAsset,
            ListUserAssetsUseCase listUserAssets,
            RegisterUserAssetPriceUseCase registerUserAssetPrice,
            AssetResponseMapper responseMapper
    ) {
        this.searchSharedAssets = searchSharedAssets;
        this.getSharedAsset = getSharedAsset;
        this.listAssetSectors = listAssetSectors;
        this.registerUserAsset = registerUserAsset;
        this.updateUserAsset = updateUserAsset;
        this.getUserAsset = getUserAsset;
        this.listUserAssets = listUserAssets;
        this.registerUserAssetPrice = registerUserAssetPrice;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/shared-assets")
    public List<AssetApiDTOs.SharedAssetResponseDTO> sharedAssets(
            @RequestParam(required = false) String search
    ) {
        return searchSharedAssets
                .search(search)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @GetMapping("/shared-assets/{id}")
    public AssetApiDTOs.SharedAssetResponseDTO sharedAsset(
            @PathVariable UUID id
    ) {
        SharedAsset asset =
                getSharedAsset.getById(id);

        return responseMapper.toResponse(asset);
    }

    @PostMapping("/user-assets")
    public ResponseEntity<AssetApiDTOs.UserAssetResponseDTO> createUserAsset(
            @Valid
            @RequestBody
            AssetApiDTOs.RegisterUserAssetRequestDTO request,
            Authentication authentication
    ) {
        UserAsset asset =
                registerUserAsset.register(
                        user(authentication),
                        new RegisterUserAssetUseCase.RegisterUserAssetCommand(
                                request.name(),
                                request.type(),
                                request.sector(),
                                request.origin(),
                                request.initialPrice()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        responseMapper.toResponse(asset)
                );
    }

    @PutMapping("/user-assets/{id}")
    public AssetApiDTOs.UserAssetResponseDTO updateUserAsset(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            AssetApiDTOs.UpdateUserAssetRequestDTO request,
            Authentication authentication
    ) {
        UserAsset asset =
                updateUserAsset.update(
                        user(authentication),
                        id,
                        new UpdateUserAssetUseCase.UpdateUserAssetCommand(
                                request.name(),
                                request.sector()
                        )
                );

        return responseMapper.toResponse(asset);
    }

    @GetMapping("/user-assets")
    public List<AssetApiDTOs.UserAssetResponseDTO> userAssets(
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        return listUserAssets
                .list(
                        user(authentication),
                        search
                )
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @GetMapping("/user-assets/{id}")
    public AssetApiDTOs.UserAssetResponseDTO userAsset(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UserAsset asset =
                getUserAsset.getById(
                        user(authentication),
                        id
                );

        return responseMapper.toResponse(asset);
    }

    @PostMapping("/user-assets/{id}/prices")
    public ResponseEntity<AssetApiDTOs.UserAssetPriceResponseDTO> registerPrice(
            @PathVariable UUID id,
            @Valid
            @RequestBody
            AssetApiDTOs.RegisterUserAssetPriceRequestDTO request,
            Authentication authentication
    ) {
        UserAssetPrice price =
                registerUserAssetPrice.register(
                        user(authentication),
                        id,
                        new RegisterUserAssetPriceUseCase.RegisterUserAssetPriceCommand(
                                request.unitPrice()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        responseMapper.toResponse(price)
                );
    }

    @GetMapping("/sectors")
    public List<AssetApiDTOs.SectorResponseDTO> sectors() {
        return listAssetSectors
                .list()
                .stream()
                .map(this::sector)
                .toList();
    }

    private AssetApiDTOs.SectorResponseDTO sector(
            AssetSector sector
    ) {
        return new AssetApiDTOs.SectorResponseDTO(
                sector.code(),
                sector.displayName(),
                sector.custom()
        );
    }

    private UUID user(
            Authentication authentication
    ) {
        return ((JwtUser) authentication.getPrincipal())
                .getId();
    }
}