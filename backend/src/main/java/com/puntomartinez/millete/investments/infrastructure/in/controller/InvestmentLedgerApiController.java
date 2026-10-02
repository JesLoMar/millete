package com.puntomartinez.millete.investments.infrastructure.in.controller;

import com.puntomartinez.millete.investments.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentLedgerSnapshotUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.RestoreInvestmentLedgerUseCase;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentLedgerSnapshotApiDTOs;
import com.puntomartinez.millete.investments.infrastructure.out.persistence.postgresql.mappers.InvestmentLedgerSnapshotMapper;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments/ledger")
public class InvestmentLedgerApiController {

    private final GetInvestmentLedgerSnapshotUseCase getSnapshot;
    private final RestoreInvestmentLedgerUseCase restoreSnapshot;
    private final InvestmentLedgerSnapshotMapper snapshotMapper;

    public InvestmentLedgerApiController(
            GetInvestmentLedgerSnapshotUseCase getSnapshot,
            RestoreInvestmentLedgerUseCase restoreSnapshot,
            InvestmentLedgerSnapshotMapper snapshotMapper
    ) {
        this.getSnapshot = getSnapshot;
        this.restoreSnapshot = restoreSnapshot;
        this.snapshotMapper = snapshotMapper;
    }

    @GetMapping("/snapshot")
    public ResponseEntity<
            InvestmentLedgerSnapshotApiDTOs.InvestmentLedgerSnapshotDTO
            > snapshot(
            Authentication authentication
    ) {
        InvestmentLedgerSnapshot snapshot =
                getSnapshot.get(
                        user(authentication)
                );

        return ResponseEntity.ok(
                snapshotMapper.toResponse(snapshot)
        );
    }

    @PostMapping("/restore")
    public ResponseEntity<Void> restore(
            @RequestBody
            InvestmentLedgerSnapshotApiDTOs.InvestmentLedgerSnapshotDTO request,
            Authentication authentication
    ) {
        InvestmentLedgerSnapshot snapshot =
                snapshotMapper.toDomain(request);

        restoreSnapshot.restore(
                user(authentication),
                snapshot
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