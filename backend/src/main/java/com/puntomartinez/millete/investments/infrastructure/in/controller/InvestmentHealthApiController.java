package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.ports.in.CheckInvestmentHealthUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentHealthApiDTOs;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentHealthApiController {

    private final CheckInvestmentHealthUseCase checkHealth;

    public InvestmentHealthApiController(
            CheckInvestmentHealthUseCase checkHealth
    ) {
        this.checkHealth = checkHealth;
    }

    @GetMapping("/health")
    public InvestmentHealthApiDTOs.HealthResponseDTO health(
            Authentication authentication
    ) {
        CheckInvestmentHealthUseCase.HealthReport report =
                checkHealth.check(
                        user(authentication)
                );

        List<InvestmentHealthApiDTOs.HealthIssueResponseDTO> issues =
                report.issues()
                        .stream()
                        .map(issue ->
                                new InvestmentHealthApiDTOs.HealthIssueResponseDTO(
                                        issue.code(),
                                        issue.resourceId(),
                                        issue.severity(),
                                        issue.message()
                                )
                        )
                        .toList();

        return new InvestmentHealthApiDTOs.HealthResponseDTO(
                report.checkedAt(),
                issues
        );
    }

    private UUID user(
            Authentication authentication
    ) {
        return ((JwtUser) authentication.getPrincipal())
                .getId();
    }
}