package com.puntomartinez.millete.groupgoals.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.groupgoals.application.services.GroupGoalService;
import com.puntomartinez.millete.groupgoals.domain.model.GoalInvitation;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.in.*;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalUnitRepository;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
public class GroupGoalController {

    private final GroupGoalService groupGoalService;
    private final CreateGoalUnitUseCase createGoalUnitUseCase;
    private final CalculateContributionsUseCase calculateContributionsUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final DeleteGoalUnitUseCase deleteGoalUnitUseCase;
    private final GoalUnitRepository goalUnitRepository;
    private final UserRepository userRepository;

    public GroupGoalController(
            GroupGoalService groupGoalService,
            CreateGoalUnitUseCase createGoalUnitUseCase,
            CalculateContributionsUseCase calculateContributionsUseCase,
            AcceptInvitationUseCase acceptInvitationUseCase,
            DeleteGoalUnitUseCase deleteGoalUnitUseCase,
            GoalUnitRepository goalUnitRepository,
            UserRepository userRepository) {
        this.groupGoalService = groupGoalService;
        this.createGoalUnitUseCase = createGoalUnitUseCase;
        this.calculateContributionsUseCase = calculateContributionsUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.deleteGoalUnitUseCase = deleteGoalUnitUseCase;
        this.goalUnitRepository = goalUnitRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<GoalListItemResponseDTO>> getMyGoals(Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        List<GoalListItemResponseDTO> goals = groupGoalService.getGoalsByUserId(userId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponseDTO> getGoalDetail(
            @PathVariable UUID goalId,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        GoalDetailResponseDTO detail = groupGoalService.getGoalDetail(goalId, userId);
        return ResponseEntity.ok(detail);
    }

    @PostMapping
    public ResponseEntity<GoalResponseDTO> createGoal(
            @RequestBody CreateGoalRequestDTO request,
            Authentication authentication) {

        UUID adminId = ((JwtUser) authentication.getPrincipal()).getId();

        GoalUnit goal = createGoalUnitUseCase.createGoalUnit(
                adminId,
                request.getName(),
                request.getMonthlyTarget(),
                request.getDistributionMode()
        );

        GoalResponseDTO response = new GoalResponseDTO();
        response.setId(goal.getId());
        response.setName(goal.getName());
        response.setMonthlyTarget(goal.getMonthlyTarget());
        response.setDistributionMode(goal.getDistributionMode().name());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<Void> updateGoal(
            @PathVariable UUID goalId,
            @RequestBody UpdateGoalRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        groupGoalService.updateGoal(goalId, userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable UUID goalId,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        deleteGoalUnitUseCase.deleteGoalUnit(goalId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{goalId}/members/{memberId}")
    public ResponseEntity<Void> updateMember(
            @PathVariable UUID goalId,
            @PathVariable UUID memberId,
            @RequestBody UpdateMemberRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        groupGoalService.updateMember(goalId, memberId, userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{goalId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID goalId,
            @PathVariable UUID memberId,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        groupGoalService.deleteMember(goalId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goalId}/contributions")
    public ResponseEntity<Map<UUID, BigDecimal>> getContributions(
            @PathVariable UUID goalId,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        Map<UUID, BigDecimal> contributions = calculateContributionsUseCase.calculateContributions(goalId, userId);
        return ResponseEntity.ok(contributions);
    }

    @PostMapping("/{goalId}/contributions")
    public ResponseEntity<Void> addContribution(
            @PathVariable UUID goalId,
            @RequestBody AddContributionRequestDTO request,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        groupGoalService.addContribution(goalId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{goalId}/invitations")
    public ResponseEntity<InvitationResponseDTO> inviteMember(
            @PathVariable UUID goalId,
            @RequestBody InviteMemberRequestDTO request,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        GoalInvitation invitation = groupGoalService.inviteMember(goalId, userId, request.getIdentifier());

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToInvitationResponse(invitation));
    }

    @GetMapping("/invitations/pending")
    public ResponseEntity<List<InvitationResponseDTO>> getPendingInvitations(Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        List<GoalInvitation> invitations = groupGoalService.getPendingInvitations(userId);
        List<InvitationResponseDTO> response = invitations.stream()
                .map(this::mapToInvitationResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable UUID invitationId,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        acceptInvitationUseCase.acceptInvitation(userId, invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @PathVariable UUID invitationId,
            Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        groupGoalService.rejectInvitation(userId, invitationId);
        return ResponseEntity.ok().build();
    }

    private InvitationResponseDTO mapToInvitationResponse(GoalInvitation inv) {
        InvitationResponseDTO dto = new InvitationResponseDTO();
        dto.setId(inv.getId());
        dto.setFamilyId(inv.getGoalId());
        dto.setInviterUserId(inv.getInviterUserId());
        dto.setInvitedUserId(inv.getInvitedUserId());
        dto.setStatus(inv.getStatus().name());
        dto.setCreatedAt(inv.getCreatedAt());

        goalUnitRepository.findById(inv.getGoalId())
                .ifPresent(f -> dto.setFamilyName(f.getName()));

        if (inv.getInviterUserId() != null) {
            userRepository.findById(inv.getInviterUserId())
                    .ifPresent(u -> dto.setInviterName(
                            u.getUsername() != null ? u.getUsername() : u.getEmail()));
        }

        return dto;
    }
}