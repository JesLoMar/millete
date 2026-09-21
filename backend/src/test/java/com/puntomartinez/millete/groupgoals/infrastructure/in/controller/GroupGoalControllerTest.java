package com.puntomartinez.millete.groupgoals.infrastructure.in.controller;

import com.puntomartinez.millete.groupgoals.domain.model.DistributionMode;
import com.puntomartinez.millete.groupgoals.domain.model.GoalRole;
import com.puntomartinez.millete.groupgoals.domain.model.GoalUnit;
import com.puntomartinez.millete.groupgoals.domain.ports.in.*;
import com.puntomartinez.millete.groupgoals.domain.ports.out.UserLookupPort;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupGoalController")
class GroupGoalControllerTest {

    @Mock
    private CreateGoalUnitUseCase createGoalUnitUseCase;

    @Mock
    private UpdateGoalUseCase updateGoalUseCase;

    @Mock
    private DeleteGoalUnitUseCase deleteGoalUnitUseCase;

    @Mock
    private UpdateMemberUseCase updateMemberUseCase;

    @Mock
    private DeleteMemberUseCase deleteMemberUseCase;

    @Mock
    private AddContributionUseCase addContributionUseCase;

    @Mock
    private WithdrawContributionUseCase withdrawContributionUseCase;

    @Mock
    private LeaveGoalUseCase leaveGoalUseCase;

    @Mock
    private ListGoalsUseCase listGoalsUseCase;

    @Mock
    private GetGoalDetailUseCase getGoalDetailUseCase;

    @Mock
    private GetContributionHistoryUseCase getContributionHistoryUseCase;

    @Mock
    private CalculateContributionsUseCase calculateContributionsUseCase;

    @Mock
    private InviteMemberUseCase inviteMemberUseCase;

    @Mock
    private ListPendingInvitationsUseCase listPendingInvitationsUseCase;

    @Mock
    private AcceptInvitationUseCase acceptInvitationUseCase;

    @Mock
    private RejectInvitationUseCase rejectInvitationUseCase;

    @Mock
    private UserLookupPort userLookupPort;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GroupGoalController controller;

    private final UUID userId = UUID.randomUUID();
    private final UUID goalId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        JwtUser jwtUser = new JwtUser(userId, "testuser", "test@example.com");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    @Test
    @DisplayName("Should create goal and return 201")
    void shouldCreateGoalAndReturn201() {
        GoalUnit goal = GoalUnit.reconstitute(
                goalId, "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE,
                LocalDateTime.now(), LocalDateTime.now(), true
        );

        when(createGoalUnitUseCase.create(any()))
                .thenReturn(goal);

        CreateGoalRequestDTO request = new CreateGoalRequestDTO(
                "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE
        );

        ResponseEntity<GoalResponseDTO> response =
                controller.createGoal(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Family trip");
        assertThat(response.getBody().monthlyTarget())
                .isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("Should list goals")
    void shouldListGoals() {
        ListGoalsUseCase.GoalSummary summary = new ListGoalsUseCase.GoalSummary(
                goalId, "Family trip", new BigDecimal("300.00"),
                DistributionMode.EQUITATIVE, true, LocalDateTime.now()
        );

        when(listGoalsUseCase.listGoals(userId))
                .thenReturn(List.of(summary));

        ResponseEntity<List<GoalListItemResponseDTO>> response =
                controller.getMyGoals(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst().name()).isEqualTo("Family trip");
    }

    @Test
    @DisplayName("Should update goal and return 200")
    void shouldUpdateGoalAndReturn200() {
        when(updateGoalUseCase.update(eq(goalId), eq(userId), any()))
                .thenReturn(null);

        UpdateGoalRequestDTO request = new UpdateGoalRequestDTO(
                "Updated trip", new BigDecimal("500.00"),
                DistributionMode.CUSTOM
        );

        ResponseEntity<Void> response =
                controller.updateGoal(goalId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(updateGoalUseCase).update(eq(goalId), eq(userId), any());
    }

    @Test
    @DisplayName("Should delete goal and return 204")
    void shouldDeleteGoalAndReturn204() {
        ResponseEntity<Void> response =
                controller.deleteGoal(goalId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteGoalUnitUseCase).deleteGoalUnit(goalId, userId);
    }

    @Test
    @DisplayName("Should add contribution and return 201")
    void shouldAddContributionAndReturn201() {
        AddContributionRequestDTO request =
                new AddContributionRequestDTO(new BigDecimal("100.00"));

        ResponseEntity<Void> response =
                controller.addContribution(goalId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(addContributionUseCase).addContribution(
                eq(goalId), eq(userId), any()
        );
    }

    @Test
    @DisplayName("Should withdraw contribution and return 200")
    void shouldWithdrawContributionAndReturn200() {
        WithdrawContributionRequestDTO request =
                new WithdrawContributionRequestDTO(new BigDecimal("50.00"));

        ResponseEntity<Void> response =
                controller.withdrawContribution(goalId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(withdrawContributionUseCase).withdrawContribution(
                eq(goalId), eq(userId), any()
        );
    }

    @Test
    @DisplayName("Should leave goal and return 204")
    void shouldLeaveGoalAndReturn204() {
        ResponseEntity<Void> response =
                controller.leaveGoal(goalId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(leaveGoalUseCase).leaveGoal(goalId, userId);
    }

    @Test
    @DisplayName("Should invite member and return 201")
    void shouldInviteMemberAndReturn201() {
        InviteMemberUseCase.InvitationResult invitationResult =
                new InviteMemberUseCase.InvitationResult(
                        UUID.randomUUID(), goalId, "Family trip",
                        userId, "testuser", UUID.randomUUID(),
                        com.puntomartinez.millete.groupgoals.domain.model.InvitationStatus.PENDING,
                        LocalDateTime.now()
                );

        when(inviteMemberUseCase.inviteMember(eq(goalId), eq(userId), any()))
                .thenReturn(invitationResult);

        InviteMemberRequestDTO request =
                new InviteMemberRequestDTO("invited@mail.com");

        ResponseEntity<InvitationResponseDTO> response =
                controller.inviteMember(goalId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().goalName()).isEqualTo("Family trip");
    }

    @Test
    @DisplayName("Should accept invitation and return 200")
    void shouldAcceptInvitationAndReturn200() {
        UUID invitationId = UUID.randomUUID();

        ResponseEntity<Void> response =
                controller.acceptInvitation(invitationId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(acceptInvitationUseCase).acceptInvitation(userId, invitationId);
    }

    @Test
    @DisplayName("Should reject invitation and return 200")
    void shouldRejectInvitationAndReturn200() {
        UUID invitationId = UUID.randomUUID();

        ResponseEntity<Void> response =
                controller.rejectInvitation(invitationId, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(rejectInvitationUseCase).rejectInvitation(userId, invitationId);
    }
}