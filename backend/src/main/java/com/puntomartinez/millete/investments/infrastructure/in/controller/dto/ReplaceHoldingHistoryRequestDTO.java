package com.puntomartinez.millete.investments.infrastructure.in.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceHoldingHistoryRequestDTO(
        @NotEmpty List<@Valid RecordActivityRequestDTO> activities
) { }
