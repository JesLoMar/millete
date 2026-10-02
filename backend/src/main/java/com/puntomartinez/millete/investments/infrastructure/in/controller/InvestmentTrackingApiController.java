package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.ports.in.ConfigureInvestmentTrackingUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentTrackingApiDTOs;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/investments/tracking")
public class InvestmentTrackingApiController {

    private final ConfigureInvestmentTrackingUseCase configureTracking;

    public InvestmentTrackingApiController(
            ConfigureInvestmentTrackingUseCase configureTracking
    ) {
        this.configureTracking = configureTracking;
    }

    @PostMapping
    public ResponseEntity<Void> configure(
            @Valid @RequestBody
            InvestmentTrackingApiDTOs.ConfigureInvestmentTrackingRequestDTO request,
            Authentication authentication
    ) {
        configureTracking.configure(
                user(authentication),
                request.trackingStartAt()
        );

        return ResponseEntity.noContent().build();
    }

    private java.util.UUID user(
            Authentication authentication
    ) {
        return ((JwtUser) authentication.getPrincipal())
                .getId();
    }
}