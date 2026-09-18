package com.puntomartinez.millete.groupgoals.infrastructure.in.controller;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.domain.ports.in.AcceptInvitationUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.AddContributionUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.CalculateContributionsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.CreateGoalUnitUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.DeleteGoalUnitUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.DeleteMemberUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetContributionHistoryUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetGoalDetailUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.InviteMemberUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.LeaveGoalUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListGoalsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListPendingInvitationsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.RejectInvitationUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.UpdateGoalUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.UpdateMemberUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.WithdrawContributionUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository.MemberContributionTotals;
import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.AddContributionRequestDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.ContributionsCalculationResponseDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.CreateGoalRequestDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.GoalContributionDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.GoalDetailResponseDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.GoalListItemResponseDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.GoalMemberDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.GoalResponseDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.InvitationResponseDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.InviteMemberRequestDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.UpdateGoalRequestDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.UpdateMemberRequestDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.WithdrawContributionRequestDTO;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/goals")
@PreAuthorize("isAuthenticated()")
public class GroupGoalController {

    private final CreateGoalUnitUseCase createGoalUnitUseCase;
    private final UpdateGoalUseCase updateGoalUseCase;
    private final DeleteGoalUnitUseCase deleteGoalUnitUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final DeleteMemberUseCase deleteMemberUseCase;
    private final AddContributionUseCase addContributionUseCase;
    private final WithdrawContributionUseCase withdrawContributionUseCase;
    private final LeaveGoalUseCase leaveGoalUseCase;
    private final ListGoalsUseCase listGoalsUseCase;
    private final GetGoalDetailUseCase getGoalDetailUseCase;
    private final GetContributionHistoryUseCase getContributionHistoryUseCase;
    private final CalculateContributionsUseCase calculateContributionsUseCase;
    private final InviteMemberUseCase inviteMemberUseCase;
    private final ListPendingInvitationsUseCase listPendingInvitationsUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final RejectInvitationUseCase rejectInvitationUseCase;
    private final UserLookupPort userLookupPort;

    public GroupGoalController(
            CreateGoalUnitUseCase createGoalUnitUseCase,
            UpdateGoalUseCase updateGoalUseCase,
            DeleteGoalUnitUseCase deleteGoalUnitUseCase,
            UpdateMemberUseCase updateMemberUseCase,
            DeleteMemberUseCase deleteMemberUseCase,
            AddContributionUseCase addContributionUseCase,
            WithdrawContributionUseCase withdrawContributionUseCase,
            LeaveGoalUseCase leaveGoalUseCase,
            ListGoalsUseCase listGoalsUseCase,
            GetGoalDetailUseCase getGoalDetailUseCase,
            GetContributionHistoryUseCase getContributionHistoryUseCase,
            CalculateContributionsUseCase calculateContributionsUseCase,
            InviteMemberUseCase inviteMemberUseCase,
            ListPendingInvitationsUseCase listPendingInvitationsUseCase,
            AcceptInvitationUseCase acceptInvitationUseCase,
            RejectInvitationUseCase rejectInvitationUseCase,
            UserLookupPort userLookupPort
    ) {
        this.createGoalUnitUseCase = createGoalUnitUseCase;
        this.updateGoalUseCase = updateGoalUseCase;
        this.deleteGoalUnitUseCase = deleteGoalUnitUseCase;
        this.updateMemberUseCase = updateMemberUseCase;
        this.deleteMemberUseCase = deleteMemberUseCase;
        this.addContributionUseCase = addContributionUseCase;
        this.withdrawContributionUseCase = withdrawContributionUseCase;
        this.leaveGoalUseCase = leaveGoalUseCase;
        this.listGoalsUseCase = listGoalsUseCase;
        this.getGoalDetailUseCase = getGoalDetailUseCase;
        this.getContributionHistoryUseCase = getContributionHistoryUseCase;
        this.calculateContributionsUseCase = calculateContributionsUseCase;
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.listPendingInvitationsUseCase = listPendingInvitationsUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.rejectInvitationUseCase = rejectInvitationUseCase;
        this.userLookupPort = userLookupPort;
    }

    @GetMapping
    public ResponseEntity<List<GoalListItemResponseDTO>> getMyGoals(
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        List<ListGoalsUseCase.GoalSummary> goals =
                listGoalsUseCase.listGoals(userId);

        List<GoalListItemResponseDTO> response =
                goals.stream()
                        .map(this::mapGoalSummary)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponseDTO> getGoalDetail(
            @PathVariable UUID goalId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        GetGoalDetailUseCase.GoalDetail detail =
                getGoalDetailUseCase.getGoalDetail(goalId, userId);

        return ResponseEntity.ok(mapGoalDetail(detail, userId));
    }

    @PostMapping
    public ResponseEntity<GoalResponseDTO> createGoal(
            @Valid @RequestBody CreateGoalRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        CreateGoalUnitUseCase.CreateGoalUnitCommand command =
                new CreateGoalUnitUseCase.CreateGoalUnitCommand(
                        userId,
                        request.name(),
                        request.monthlyTarget(),
                        request.distributionMode()
                );

        var goal = createGoalUnitUseCase.create(command);

        GoalResponseDTO response = new GoalResponseDTO(
                goal.getId(),
                goal.getName(),
                goal.getMonthlyTarget(),
                goal.getDistributionMode().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<Void> updateGoal(
            @PathVariable UUID goalId,
            @Valid @RequestBody UpdateGoalRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        UpdateGoalUseCase.UpdateGoalCommand command =
                new UpdateGoalUseCase.UpdateGoalCommand(
                        request.name(),
                        request.monthlyTarget(),
                        request.distributionMode()
                );

        updateGoalUseCase.update(goalId, userId, command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable UUID goalId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        deleteGoalUnitUseCase.deleteGoalUnit(goalId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{goalId}/members/{memberId}")
    public ResponseEntity<Void> updateMember(
            @PathVariable UUID goalId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        UpdateMemberUseCase.UpdateMemberCommand command =
                new UpdateMemberUseCase.UpdateMemberCommand(
                        request.role(),
                        request.salary(),
                        request.customPercentage()
                );

        updateMemberUseCase.updateMember(goalId, memberId, userId, command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{goalId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID goalId,
            @PathVariable UUID memberId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        deleteMemberUseCase.deleteMember(goalId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goalId}/contributions")
    public ResponseEntity<PaginatedResponseDTO<GoalContributionDTO>> getContributions(
            @PathVariable UUID goalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "60") int size,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        GetContributionHistoryUseCase.PaginatedContributions pageResult =
                getContributionHistoryUseCase.getContributionHistory(
                        goalId, userId, page, size
                );

        Set<UUID> contributorIds = pageResult.contributions().stream()
                .map(GoalContribution::getUserId)
                .collect(Collectors.toSet());

        Map<UUID, UserLookupPort.UserInfo> usersById =
                userLookupPort.findByIds(contributorIds);

        List<GoalContributionDTO> response =
                pageResult.contributions().stream()
                        .map(c -> mapContribution(c, usersById))
                        .toList();

        return ResponseEntity.ok(
                new PaginatedResponseDTO<>(
                        response,
                        pageResult.currentPage(),
                        pageResult.totalPages(),
                        pageResult.totalElements(),
                        pageResult.size(),
                        pageResult.first(),
                        pageResult.last()
                )
        );
    }

    @PostMapping("/{goalId}/contributions")
    public ResponseEntity<Void> addContribution(
            @PathVariable UUID goalId,
            @Valid @RequestBody AddContributionRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        addContributionUseCase.addContribution(
                goalId, userId,
                new AddContributionUseCase.AddContributionCommand(request.amount())
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{goalId}/contributions/withdraw")
    public ResponseEntity<Void> withdrawContribution(
            @PathVariable UUID goalId,
            @Valid @RequestBody WithdrawContributionRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        withdrawContributionUseCase.withdrawContribution(
                goalId, userId,
                new WithdrawContributionUseCase.WithdrawContributionCommand(request.amount())
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{goalId}/leave")
    public ResponseEntity<Void> leaveGoal(
            @PathVariable UUID goalId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        leaveGoalUseCase.leaveGoal(goalId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goalId}/contributions/calculation")
    public ResponseEntity<ContributionsCalculationResponseDTO> calculateContributions(
            @PathVariable UUID goalId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        CalculateContributionsUseCase.ContributionsCalculation calculation =
                calculateContributionsUseCase.calculateContributions(goalId, userId);

        return ResponseEntity.ok(mapCalculation(calculation));
    }

    @PostMapping("/{goalId}/invitations")
    public ResponseEntity<InvitationResponseDTO> inviteMember(
            @PathVariable UUID goalId,
            @Valid @RequestBody InviteMemberRequestDTO request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        InviteMemberUseCase.InviteMemberCommand command =
                new InviteMemberUseCase.InviteMemberCommand(request.identifier());

        InviteMemberUseCase.InvitationResult invitation =
                inviteMemberUseCase.inviteMember(goalId, userId, command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapInvitationResponse(invitation));
    }

    @GetMapping("/invitations/pending")
    public ResponseEntity<List<InvitationResponseDTO>> getPendingInvitations(
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        List<ListPendingInvitationsUseCase.InvitationResult> invitations =
                listPendingInvitationsUseCase.listPendingInvitations(userId);

        List<InvitationResponseDTO> response =
                invitations.stream()
                        .map(this::mapInvitationResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable UUID invitationId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        acceptInvitationUseCase.acceptInvitation(userId, invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @PathVariable UUID invitationId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        rejectInvitationUseCase.rejectInvitation(userId, invitationId);
        return ResponseEntity.ok().build();
    }

    // ── Mappers ──────────────────────────────────────────────

    private GoalListItemResponseDTO mapGoalSummary(
            ListGoalsUseCase.GoalSummary summary
    ) {
        return new GoalListItemResponseDTO(
                summary.id(),
                summary.name(),
                summary.monthlyTarget(),
                summary.admin()
        );
    }

    private GoalDetailResponseDTO mapGoalDetail(
            GetGoalDetailUseCase.GoalDetail detail,
            UUID userId
    ) {
        boolean isAdmin = detail.members().stream()
                .anyMatch(m ->
                        m.userId().equals(userId)
                                && m.role() == GoalRole.ADMIN
                );

        List<GoalMemberDTO> members = detail.members().stream()
                .map(m -> new GoalMemberDTO(
                        m.id(),
                        m.userId(),
                        resolveDisplayName(m.username(), m.email()),
                        m.role().name(),
                        m.salary(),
                        m.customPercentage()
                ))
                .toList();

        Map<UUID, BigDecimal> contributionTotals = detail.totals().stream()
                .collect(Collectors.toMap(
                        MemberContributionTotals::userId,
                        MemberContributionTotals::net
                ));

        return new GoalDetailResponseDTO(
                detail.id(),
                detail.name(),
                detail.monthlyTarget(),
                detail.distributionMode().name(),
                isAdmin,
                members,
                contributionTotals
        );
    }

    private GoalContributionDTO mapContribution(
            GoalContribution contribution,
            Map<UUID, UserLookupPort.UserInfo> usersById
    ) {
        UserLookupPort.UserInfo userInfo =
                usersById.get(contribution.getUserId());

        String userName = resolveDisplayName(
                userInfo != null ? userInfo.username() : null,
                userInfo != null ? userInfo.email() : null
        );

        return new GoalContributionDTO(
                contribution.getId(),
                contribution.getUserId(),
                userName,
                contribution.getAmount(),
                contribution.getDate()
        );
    }

    private InvitationResponseDTO mapInvitationResponse(
            InviteMemberUseCase.InvitationResult invitation
    ) {
        return new InvitationResponseDTO(
                invitation.id(),
                invitation.goalId(),
                invitation.goalName(),
                invitation.inviterUserId(),
                invitation.inviterName(),
                invitation.invitedUserId(),
                invitation.status().name(),
                invitation.createdAt()
        );
    }

    private InvitationResponseDTO mapInvitationResponse(
            ListPendingInvitationsUseCase.InvitationResult invitation
    ) {
        return new InvitationResponseDTO(
                invitation.id(),
                invitation.goalId(),
                invitation.goalName(),
                invitation.inviterUserId(),
                invitation.inviterName(),
                invitation.invitedUserId(),
                invitation.status().name(),
                invitation.createdAt()
        );
    }

    private ContributionsCalculationResponseDTO mapCalculation(
            CalculateContributionsUseCase.ContributionsCalculation calculation
    ) {
        List<ContributionsCalculationResponseDTO.MemberContributionDTO> contributions =
                calculation.contributions().stream()
                        .map(mc -> new ContributionsCalculationResponseDTO.MemberContributionDTO(
                                mc.userId(),
                                mc.suggestedAmount()
                        ))
                        .toList();

        return new ContributionsCalculationResponseDTO(
                calculation.monthlyTarget(),
                calculation.distributionMode(),
                contributions
        );
    }

    private String resolveDisplayName(String username, String email) {
        if (username != null && !username.isBlank()) {
            return username;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return "Usuario";
    }

    private UUID getUserId(Authentication authentication) {
        return ((JwtUser) authentication.getPrincipal()).getId();
    }
}