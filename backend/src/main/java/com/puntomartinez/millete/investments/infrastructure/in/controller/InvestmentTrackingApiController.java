package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.ports.in.ConfigureInvestmentTrackingUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentTrackingUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentTrackingApiDTOs;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments/tracking")
public class InvestmentTrackingApiController {

    private final ConfigureInvestmentTrackingUseCase configureTracking;
    private final GetInvestmentTrackingUseCase getTracking;

    public InvestmentTrackingApiController(
            ConfigureInvestmentTrackingUseCase configureTracking,
            GetInvestmentTrackingUseCase getTracking
    ) {
        this.configureTracking = configureTracking;
        this.getTracking = getTracking;
    }

    @GetMapping
    public ResponseEntity<InvestmentTrackingApiDTOs.InvestmentTrackingResponseDTO> get(
            Authentication authentication
    ) {
        Optional<Instant> trackingStartAt =
                getTracking.getTrackingStartAt(user(authentication));

        return ResponseEntity.ok(
                new InvestmentTrackingApiDTOs.InvestmentTrackingResponseDTO(
                        trackingStartAt.isPresent(),
                        trackingStartAt.orElse(null)
                )
        );
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

    private UUID user(
            Authentication authentication
    ) {
        return ((JwtUser) authentication.getPrincipal())
                .getId();
    }
}