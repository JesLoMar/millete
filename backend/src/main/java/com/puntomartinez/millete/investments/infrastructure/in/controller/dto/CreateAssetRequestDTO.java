package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import com.puntomartinez.millete.investments.domain.model.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateAssetRequestDTO(@NotBlank @Size(max=120) String name,
                                    @Size(max=40) String symbol,
                                    @NotNull AssetType type,
                                    UUID sectorId,
                                    @NotBlank @Pattern(regexp="[A-Za-z]{3}") String currency) { }
