package com.puntomartinez.millete.groupgoals.infrastructure.in.controller;

import com.puntomartinez.millete.groupgoals.domain.ports.in.AcceptInvitationUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.AddContributionUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.CreateGoalUnitUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.DeleteGoalUnitUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.DeleteMemberUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetContributionHistoryUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.GetGoalDetailUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.InviteMemberUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListGoalsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.ListPendingInvitationsUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.RejectInvitationUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.UpdateGoalUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.in.UpdateMemberUseCase;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.AddContributionRequestDTO;
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
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.PaginatedResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
public class GroupGoalController {

    private final CreateGoalUnitUseCase createGoalUnitUseCase;
    private final UpdateGoalUseCase updateGoalUseCase;
    private final DeleteGoalUnitUseCase deleteGoalUnitUseCase;
    private final UpdateMemberUseCase updateMemberUseCase;
    private final DeleteMemberUseCase deleteMemberUseCase;
    private final AddContributionUseCase addContributionUseCase;

    private final ListGoalsUseCase listGoalsUseCase;
    private final GetGoalDetailUseCase getGoalDetailUseCase;
    private final GetContributionHistoryUseCase getContributionHistoryUseCase;

    private final InviteMemberUseCase inviteMemberUseCase;
    private final ListPendingInvitationsUseCase listPendingInvitationsUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final RejectInvitationUseCase rejectInvitationUseCase;

    public GroupGoalController(
            CreateGoalUnitUseCase createGoalUnitUseCase,
            UpdateGoalUseCase updateGoalUseCase,
            DeleteGoalUnitUseCase deleteGoalUnitUseCase,
            UpdateMemberUseCase updateMemberUseCase,
            DeleteMemberUseCase deleteMemberUseCase,
            AddContributionUseCase addContributionUseCase,
            ListGoalsUseCase listGoalsUseCase,
            GetGoalDetailUseCase getGoalDetailUseCase,
            GetContributionHistoryUseCase getContributionHistoryUseCase,
            InviteMemberUseCase inviteMemberUseCase,
            ListPendingInvitationsUseCase listPendingInvitationsUseCase,
            AcceptInvitationUseCase acceptInvitationUseCase,
            RejectInvitationUseCase rejectInvitationUseCase) {

        this.createGoalUnitUseCase = createGoalUnitUseCase;
        this.updateGoalUseCase = updateGoalUseCase;
        this.deleteGoalUnitUseCase = deleteGoalUnitUseCase;
        this.updateMemberUseCase = updateMemberUseCase;
        this.deleteMemberUseCase = deleteMemberUseCase;
        this.addContributionUseCase = addContributionUseCase;

        this.listGoalsUseCase = listGoalsUseCase;
        this.getGoalDetailUseCase = getGoalDetailUseCase;
        this.getContributionHistoryUseCase = getContributionHistoryUseCase;

        this.inviteMemberUseCase = inviteMemberUseCase;
        this.listPendingInvitationsUseCase = listPendingInvitationsUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.rejectInvitationUseCase = rejectInvitationUseCase;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDTO<GoalListItemResponseDTO>> getMyGoals(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        UUID userId = getUserId(authentication);

        ListGoalsUseCase.GoalsPage pageResult =
                listGoalsUseCase.listGoals(userId, page, size);

        int totalPages = pageResult.totalPages();

        int safePage = Math.min(
                Math.max(page, 0),
                Math.max(0, totalPages - 1)
        );

        List<GoalListItemResponseDTO> response =
                pageResult.goals()
                        .stream()
                        .map(this::mapGoalSummary)
                        .toList();

        return ResponseEntity.ok(
                new PaginatedResponseDTO<>(
                        response,
                        safePage,
                        totalPages,
                        pageResult.totalElements(),
                        size,
                        safePage == 0,
                        safePage >= totalPages - 1 || totalPages == 0
                )
        );
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalDetailResponseDTO> getGoalDetail(
            @PathVariable UUID goalId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        GetGoalDetailUseCase.GoalDetail detail =
                getGoalDetailUseCase.getGoalDetail(goalId, userId);

        return ResponseEntity.ok(mapGoalDetail(detail));
    }

    @PostMapping
    public ResponseEntity<GoalResponseDTO> createGoal(
            @Valid @RequestBody CreateGoalRequestDTO request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        CreateGoalUnitUseCase.CreateGoalUnitCommand command =
                new CreateGoalUnitUseCase.CreateGoalUnitCommand(
                        request.name(),
                        request.monthlyTarget(),
                        request.distributionMode()
                );

        var goal = createGoalUnitUseCase.create(userId, command);

        GoalResponseDTO response = new GoalResponseDTO(
                goal.getId(),
                goal.getName(),
                goal.getMonthlyTarget(),
                goal.getDistributionMode().name()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<Void> updateGoal(
            @PathVariable UUID goalId,
            @Valid @RequestBody UpdateGoalRequestDTO request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        UpdateGoalUseCase.UpdateGoalCommand command =
                new UpdateGoalUseCase.UpdateGoalCommand(
                        request.name(),
                        request.monthlyTarget(),
                        request.distributionMode()
                );

        updateGoalUseCase.update(
                goalId,
                userId,
                command
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable UUID goalId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        deleteGoalUnitUseCase.deleteGoalUnit(
                goalId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{goalId}/members/{memberId}")
    public ResponseEntity<Void> updateMember(
            @PathVariable UUID goalId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRequestDTO request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        UpdateMemberUseCase.UpdateMemberCommand command =
                new UpdateMemberUseCase.UpdateMemberCommand(
                        request.role(),
                        request.salary(),
                        request.customPercentage()
                );

        updateMemberUseCase.updateMember(
                goalId,
                memberId,
                userId,
                command
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{goalId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID goalId,
            @PathVariable UUID memberId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        deleteMemberUseCase.deleteMember(
                goalId,
                memberId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goalId}/contributions")
    public ResponseEntity<PaginatedResponseDTO<GoalContributionDTO>> getContributions(
            @PathVariable UUID goalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "60") int size,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        GetContributionHistoryUseCase.ContributionHistory pageResult =
                getContributionHistoryUseCase.getContributionHistory(
                        goalId,
                        userId,
                        page,
                        size
                );

        int totalPages = pageResult.totalPages();

        int safePage = Math.min(
                Math.max(page, 0),
                Math.max(0, totalPages - 1)
        );

        List<GoalContributionDTO> response =
                pageResult.contributions()
                        .stream()
                        .map(this::mapContribution)
                        .toList();

        return ResponseEntity.ok(
                new PaginatedResponseDTO<>(
                        response,
                        safePage,
                        totalPages,
                        pageResult.totalElements(),
                        size,
                        safePage == 0,
                        safePage >= totalPages - 1
                                || totalPages == 0
                )
        );
    }

    @PostMapping("/{goalId}/contributions")
    public ResponseEntity<Void> addContribution(
            @PathVariable UUID goalId,
            @Valid @RequestBody AddContributionRequestDTO request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        AddContributionUseCase.AddContributionCommand command =
                new AddContributionUseCase.AddContributionCommand(
                        request.amount()
                );

        addContributionUseCase.addContribution(
                goalId,
                userId,
                command
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/{goalId}/invitations")
    public ResponseEntity<InvitationResponseDTO> inviteMember(
            @PathVariable UUID goalId,
            @Valid @RequestBody InviteMemberRequestDTO request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        InviteMemberUseCase.InviteMemberCommand command =
                new InviteMemberUseCase.InviteMemberCommand(
                        request.identifier()
                );

        InviteMemberUseCase.InvitationResult invitation =
                inviteMemberUseCase.inviteMember(
                        goalId,
                        userId,
                        command
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapInvitationResponse(invitation));
    }

    @GetMapping("/invitations/pending")
    public ResponseEntity<List<InvitationResponseDTO>> getPendingInvitations(
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        List<ListPendingInvitationsUseCase.InvitationResult> invitations =
                listPendingInvitationsUseCase
                        .getPendingInvitations(userId);

        List<InvitationResponseDTO> response =
                invitations.stream()
                        .map(this::mapInvitationResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable UUID invitationId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        acceptInvitationUseCase.acceptInvitation(
                userId,
                invitationId
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @PathVariable UUID invitationId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        rejectInvitationUseCase.rejectInvitation(
                userId,
                invitationId
        );

        return ResponseEntity.ok().build();
    }

    private GoalListItemResponseDTO mapGoalSummary(
            ListGoalsUseCase.GoalSummary summary) {

        return new GoalListItemResponseDTO(
                summary.id(),
                summary.name(),
                summary.monthlyTarget(),
                summary.activeMembers(),
                summary.admin()
        );
    }

    private GoalDetailResponseDTO mapGoalDetail(
            GetGoalDetailUseCase.GoalDetail detail) {

        List<GoalMemberDTO> members =
                detail.members()
                        .stream()
                        .map(member -> new GoalMemberDTO(
                                member.id(),
                                member.userId(),
                                member.memberName(),
                                member.role(),
                                member.salary(),
                                member.customPercentage()
                        ))
                        .toList();

        return new GoalDetailResponseDTO(
                detail.id(),
                detail.name(),
                detail.monthlyTarget(),
                detail.distributionMode(),
                detail.admin(),
                members,
                detail.contributionTotals()
        );
    }

    private GoalContributionDTO mapContribution(
            GetContributionHistoryUseCase.Contribution contribution) {

        return new GoalContributionDTO(
                contribution.id(),
                contribution.userId(),
                contribution.userName(),
                contribution.amount(),
                contribution.date()
        );
    }

    private InvitationResponseDTO mapInvitationResponse(
            InviteMemberUseCase.InvitationResult invitation) {

        return new InvitationResponseDTO(
                invitation.id(),
                invitation.goalId(),
                invitation.goalName(),
                invitation.inviterUserId(),
                invitation.inviterName(),
                invitation.invitedUserId(),
                invitation.status(),
                invitation.createdAt()
        );
    }

    private InvitationResponseDTO mapInvitationResponse(
            ListPendingInvitationsUseCase.InvitationResult invitation) {

        return new InvitationResponseDTO(
                invitation.id(),
                invitation.goalId(),
                invitation.goalName(),
                invitation.inviterUserId(),
                invitation.inviterName(),
                invitation.invitedUserId(),
                invitation.status(),
                invitation.createdAt()
        );
    }

    private UUID getUserId(Authentication authentication) {
        return ((JwtUser) authentication.getPrincipal()).getId();
    }
}