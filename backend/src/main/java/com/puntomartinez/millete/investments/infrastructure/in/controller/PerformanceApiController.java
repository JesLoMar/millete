package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.PerformanceAttribution;
import com.puntomartinez.millete.investments.domain.ports.in.CalculatePerformanceUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.PerformanceApiDTOs;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.PerformanceResponseMapper;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments/performance")
public class PerformanceApiController {

    private final CalculatePerformanceUseCase calculatePerformance;
    private final PerformanceResponseMapper responseMapper;

    public PerformanceApiController(
            CalculatePerformanceUseCase calculatePerformance,
            PerformanceResponseMapper responseMapper
    ) {
        this.calculatePerformance = calculatePerformance;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public PerformanceApiDTOs.PerformanceResponseDTO calculate(
            @RequestParam Instant from,
            @RequestParam Instant to,
            Authentication authentication
    ) {
        PerformanceAttribution performance =
                calculatePerformance.calculate(
                        user(authentication),
                        from,
                        to
                );

        return responseMapper.toResponse(
                performance
        );
    }

    private UUID user(
            Authentication authentication
    ) {
        return ((JwtUser) authentication.getPrincipal())
                .getId();
    }
}