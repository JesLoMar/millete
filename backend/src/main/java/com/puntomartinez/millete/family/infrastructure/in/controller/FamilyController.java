package com.puntomartinez.millete.family.infrastructure.in.controller;

import com.puntomartinez.millete.family.application.services.FamilyService;
import com.puntomartinez.millete.family.domain.model.FamilyInvitation;
import com.puntomartinez.millete.family.domain.model.FamilyUnit;
import com.puntomartinez.millete.family.domain.ports.in.AcceptInvitationUseCase;
import com.puntomartinez.millete.family.domain.ports.in.CalculateFamilyContributionsUseCase;
import com.puntomartinez.millete.family.domain.ports.in.CreateFamilyUnitUseCase;
import com.puntomartinez.millete.family.domain.ports.in.InviteFamilyMemberUseCase;
import com.puntomartinez.millete.family.infrastructure.in.controller.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/families")
public class FamilyController {

    private final CreateFamilyUnitUseCase createFamilyUnitUseCase;
    private final CalculateFamilyContributionsUseCase calculateContributionsUseCase;
    private final InviteFamilyMemberUseCase inviteFamilyMemberUseCase;
    private final FamilyService familyService;
    private final AcceptInvitationUseCase acceptInvitationUseCase;

    public FamilyController(
            CreateFamilyUnitUseCase createFamilyUnitUseCase,
            CalculateFamilyContributionsUseCase calculateContributionsUseCase,
            InviteFamilyMemberUseCase inviteFamilyMemberUseCase,
            FamilyService familyService,
            AcceptInvitationUseCase acceptInvitationUseCase) {
        this.createFamilyUnitUseCase = createFamilyUnitUseCase;
        this.calculateContributionsUseCase = calculateContributionsUseCase;
        this.inviteFamilyMemberUseCase = inviteFamilyMemberUseCase;
        this.familyService = familyService;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
    }

    @GetMapping
    public ResponseEntity<List<FamilyListItemResponseDTO>> getMyFamilies(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<FamilyListItemResponseDTO> families = familyService.getFamiliesByUserId(userId);
        return ResponseEntity.ok(families);
    }

    @GetMapping("/{familyId}")
    public ResponseEntity<FamilyDetailResponseDTO> getFamilyDetail(
            @PathVariable UUID familyId,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        FamilyDetailResponseDTO detail = familyService.getFamilyDetail(familyId, userId);
        return ResponseEntity.ok(detail);
    }

    @PostMapping
    public ResponseEntity<FamilyResponseDTO> createFamily(
            @RequestBody CreateFamilyRequestDTO request,
            Authentication authentication) {

        UUID adminId = UUID.fromString(authentication.getName());

        FamilyUnit family = createFamilyUnitUseCase.createFamilyUnit(
                adminId,
                request.getName(),
                request.getMonthlyTarget(),
                request.getDistributionMode()
        );

        FamilyResponseDTO response = new FamilyResponseDTO();
        response.setId(family.getId());
        response.setName(family.getName());
        response.setMonthlyTarget(family.getMonthlyTarget());
        response.setDistributionMode(family.getDistributionMode().name());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{familyId}")
    public ResponseEntity<Void> updateFamily(
            @PathVariable UUID familyId,
            @RequestBody UpdateFamilyRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        familyService.updateFamily(familyId, userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{familyId}/invitations")
    public ResponseEntity<InvitationResponseDTO> inviteMember(
            @PathVariable UUID familyId,
            @RequestBody InviteMemberRequestDTO request,
            Authentication authentication) {

        UUID adminId = UUID.fromString(authentication.getName());

        FamilyInvitation invitation = inviteFamilyMemberUseCase.inviteMember(
                adminId,
                familyId,
                request.getEmail()
        );

        InvitationResponseDTO response = new InvitationResponseDTO();
        response.setId(invitation.getId());
        response.setEmail(invitation.getEmail());
        response.setStatus(invitation.getStatus().name());
        response.setExpiresAt(invitation.getExpiresAt());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{familyId}/members/{memberId}")
    public ResponseEntity<Void> updateMember(
            @PathVariable UUID familyId,
            @PathVariable UUID memberId,
            @RequestBody UpdateMemberRequestDTO request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        familyService.updateMember(familyId, memberId, userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{familyId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID familyId,
            @PathVariable UUID memberId,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        familyService.deleteMember(familyId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{familyId}/contributions")
    public ResponseEntity<Map<UUID, BigDecimal>> getContributions(@PathVariable UUID familyId) {
        Map<UUID, BigDecimal> contributions = calculateContributionsUseCase.calculateContributions(familyId);
        return ResponseEntity.ok(contributions);
    }

    @PostMapping("/{familyId}/contributions")
    public ResponseEntity<Void> addContribution(
            @PathVariable UUID familyId,
            @RequestBody AddContributionRequestDTO request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        familyService.addContribution(familyId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable String token,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        acceptInvitationUseCase.acceptInvitation(userId, token);
        return ResponseEntity.ok().build();
    }
}