package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.*;
import com.puntomartinez.millete.groupgoals.domain.ports.out.*;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.AddContributionRequestDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.UpdateGoalRequestDTO;
import com.puntomartinez.millete.groupgoals.infrastructure.in.controller.dto.UpdateMemberRequestDTO;
import com.puntomartinez.millete.notifications.domain.model.Notification;
import com.puntomartinez.millete.notifications.domain.model.NotificationType;
import com.puntomartinez.millete.notifications.domain.ports.in.CreateNotificationUseCase;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
import com.puntomartinez.millete.shared.domain.exception.ForbiddenOperationException;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.shared.domain.exception.ResourceAlreadyExistsException;
import com.puntomartinez.millete.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupGoalService - Servicio de metas compartidas")
class GroupGoalServiceTest {

    @Mock
    private GoalUnitRepository goalUnitRepository;

    @Mock
    private GoalMemberRepository goalMemberRepository;

    @Mock
    private GoalInvitationRepository goalInvitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoalContributionRepository contributionRepository;

    @Mock
    private CreateNotificationUseCase createNotificationUseCase;

    @Mock
    private com.puntomartinez.millete.notifications.domain.ports.in.MarkNotificationAsActionedUseCase markNotificationAsActionedUseCase;

    @Mock
    private com.puntomartinez.millete.notifications.domain.ports.out.NotificationRepository notificationRepository;

    @InjectMocks
    private GroupGoalService groupGoalService;

    private final UUID userId = UUID.randomUUID();
    private final UUID goalId = UUID.randomUUID();

    @Test
    @DisplayName("Crear meta compartida")
    void shouldCreateGoalUnit() {
        when(goalUnitRepository.save(any(GoalUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalMemberRepository.save(any(GoalMember.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalUnit result = groupGoalService.createGoalUnit(userId, "Goal García", new BigDecimal("1000.00"), DistributionMode.EQUITATIVE);

        assertThat(result.getName()).isEqualTo("Goal García");
        assertThat(result.getDistributionMode()).isEqualTo(DistributionMode.EQUITATIVE);
        assertThat(result.isActive()).isTrue();
        verify(goalUnitRepository).save(any(GoalUnit.class));
        verify(goalMemberRepository).save(any(GoalMember.class));
    }

    @Test
    @DisplayName("Invitar miembro por username")
    void shouldInviteMember() {
        GoalMember admin = mock(GoalMember.class);
        when(admin.isAdmin()).thenReturn(true);

        UUID invitedUserId = UUID.randomUUID();
        User invitedUser = mock(User.class);
        when(invitedUser.getId()).thenReturn(invitedUserId);
        when(invitedUser.getEmail()).thenReturn("invitado@mail.com");

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(admin));
        when(userRepository.findByIdentifier("invitado")).thenReturn(Optional.of(invitedUser));
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, invitedUserId)).thenReturn(Optional.empty());
        when(goalInvitationRepository.findByGoalIdAndInvitedUserIdAndStatus(eq(goalId), eq(invitedUserId), any()))
                .thenReturn(Optional.empty());
        when(goalInvitationRepository.save(any(GoalInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalUnit goalUnit = mock(GoalUnit.class);
        when(goalUnit.getName()).thenReturn("Meta de prueba");
        when(goalUnitRepository.findById(goalId)).thenReturn(Optional.of(goalUnit));

        User inviter = mock(User.class);
        when(inviter.getUsername()).thenReturn("admin");
        when(userRepository.findById(userId)).thenReturn(Optional.of(inviter));

        when(createNotificationUseCase.create(any(CreateNotificationUseCase.CreateNotificationCommand.class)))
                .thenAnswer(inv -> {
                    CreateNotificationUseCase.CreateNotificationCommand cmd = inv.getArgument(0);
                    Notification n = new Notification();
                    n.setId(UUID.randomUUID());
                    n.setUserId(cmd.userId());
                    n.setType(cmd.type());
                    n.setTitle(cmd.title());
                    n.setMessage(cmd.message());
                    n.setMetadata(cmd.metadata());
                    n.setActionRequired(cmd.actionRequired());
                    return n;
                });

        GoalInvitation result = groupGoalService.inviteMember(goalId, userId, "invitado");

        assertThat(result.getInvitedUserId()).isEqualTo(invitedUserId);
        assertThat(result.getStatus()).isEqualTo(InvitationStatus.PENDING);
        verify(goalInvitationRepository).save(any(GoalInvitation.class));
        verify(createNotificationUseCase).create(any(CreateNotificationUseCase.CreateNotificationCommand.class));
    }

    @Test
    @DisplayName("Invitar sin ser admin lanza error")
    void shouldThrowWhenNotAdminInviting() {
        GoalMember member = mock(GoalMember.class);
        when(member.isAdmin()).thenReturn(false);
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(member));

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> groupGoalService.inviteMember(goalId, userId, "invitado"))
                .withMessage("Only administrators can invite members");
    }

    @Test
    @DisplayName("Invitar a usuario que no existe lanza error")
    void shouldThrowWhenUserNotFound() {
        GoalMember admin = mock(GoalMember.class);
        when(admin.isAdmin()).thenReturn(true);
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(admin));
        when(userRepository.findByIdentifier("fantasma")).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> groupGoalService.inviteMember(goalId, userId, "fantasma"))
                .withMessage("User not found: fantasma");
    }

    @Test
    @DisplayName("Invitar a usuario que ya es miembro lanza error")
    void shouldThrowWhenAlreadyMember() {
        GoalMember admin = mock(GoalMember.class);
        when(admin.isAdmin()).thenReturn(true);

        UUID invitedUserId = UUID.randomUUID();
        User invitedUser = mock(User.class);
        when(invitedUser.getId()).thenReturn(invitedUserId);

        GoalMember existingMember = mock(GoalMember.class);
        when(existingMember.isActive()).thenReturn(true);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(admin));
        when(userRepository.findByIdentifier("invitado")).thenReturn(Optional.of(invitedUser));
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, invitedUserId)).thenReturn(Optional.of(existingMember));

        assertThatExceptionOfType(ResourceAlreadyExistsException.class)
                .isThrownBy(() -> groupGoalService.inviteMember(goalId, userId, "invitado"))
                .withMessage("User is already a member of this goal");
    }

    @Test
    @DisplayName("Aceptar invitación")
    void shouldAcceptInvitation() {
        UUID invitationId = UUID.randomUUID();
        GoalInvitation invitation = mock(GoalInvitation.class);
        when(invitation.getInvitedUserId()).thenReturn(userId);
        when(invitation.isAcceptable()).thenReturn(true);
        when(invitation.getGoalId()).thenReturn(goalId);

        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.empty());
        when(goalMemberRepository.save(any(GoalMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalInvitationRepository.save(any(GoalInvitation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.findActiveByUserIdAndTypeAndMetadataValue(any(), any(), any(), any()))
                .thenReturn(List.of());

        GoalInvitation result = groupGoalService.acceptInvitation(userId, invitationId);

        verify(invitation).markAsAccepted();
        verify(goalMemberRepository).save(any(GoalMember.class));
        assertThat(result.getGoalId()).isEqualTo(goalId);
    }

    @Test
    @DisplayName("Aceptar invitación que no es para el usuario lanza error")
    void shouldThrowWhenInvitationNotForUser() {
        UUID invitationId = UUID.randomUUID();
        GoalInvitation invitation = mock(GoalInvitation.class);
        when(invitation.getInvitedUserId()).thenReturn(UUID.randomUUID());
        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> groupGoalService.acceptInvitation(userId, invitationId))
                .withMessage("This invitation is not for you");
    }

    @Test
    @DisplayName("Aceptar invitación expirada lanza error")
    void shouldThrowWhenInvitationExpired() {
        UUID invitationId = UUID.randomUUID();
        GoalInvitation invitation = mock(GoalInvitation.class);
        when(invitation.getInvitedUserId()).thenReturn(userId);
        when(invitation.isAcceptable()).thenReturn(false);
        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

        assertThatExceptionOfType(InvalidInputException.class)
                .isThrownBy(() -> groupGoalService.acceptInvitation(userId, invitationId))
                .withMessage("The invitation is not valid or has expired");
    }

    @Test
    @DisplayName("Rechazar invitación")
    void shouldRejectInvitation() {
        UUID invitationId = UUID.randomUUID();
        GoalInvitation invitation = mock(GoalInvitation.class);
        when(invitation.getInvitedUserId()).thenReturn(userId);
        when(invitation.getStatus()).thenReturn(InvitationStatus.PENDING);
        when(goalInvitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(goalInvitationRepository.save(any(GoalInvitation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.findActiveByUserIdAndTypeAndMetadataValue(any(), any(), any(), any()))
                .thenReturn(List.of());

        groupGoalService.rejectInvitation(userId, invitationId);

        verify(invitation).markAsRejected();
        verify(goalInvitationRepository).save(invitation);
    }

    @Test
    @DisplayName("Obtener invitaciones pendientes")
    void shouldGetPendingInvitations() {
        GoalInvitation inv = mock(GoalInvitation.class);
        when(goalInvitationRepository.findByInvitedUserIdAndStatus(userId, InvitationStatus.PENDING))
                .thenReturn(List.of(inv));

        var result = groupGoalService.getPendingInvitations(userId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Añadir aportación")
    void shouldAddContribution() {
        AddContributionRequestDTO request = new AddContributionRequestDTO();
        request.setAmount(new BigDecimal("100.00"));

        GoalMember member = mock(GoalMember.class);
        when(member.isActive()).thenReturn(true);
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(member));
        when(contributionRepository.save(any(GoalContribution.class))).thenAnswer(inv -> inv.getArgument(0));

        groupGoalService.addContribution(goalId, userId, request);

        verify(contributionRepository).save(any(GoalContribution.class));
    }

    @Test
    @DisplayName("Calcular contribuciones")
    void shouldCalculateContributions() {
        GoalUnit goal = mock(GoalUnit.class);
        GoalMember m1 = mock(GoalMember.class);
        when(m1.isActive()).thenReturn(true);
        when(goalUnitRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(m1));
        when(goalMemberRepository.findByGoalId(goalId)).thenReturn(List.of(m1));

        groupGoalService.calculateContributions(goalId, userId);

        verify(goal).setMembers(List.of(m1));
        verify(goal).calculateContributions();
    }

    @Test
    @DisplayName("Obtener metas por usuario")
    void shouldGetGoalsByUserId() {
        GoalMember membership = mock(GoalMember.class);
        when(membership.getGoalId()).thenReturn(goalId);
        when(membership.getUserId()).thenReturn(userId);
        when(membership.isAdmin()).thenReturn(true);

        GoalUnit goal = mock(GoalUnit.class);
        when(goal.getId()).thenReturn(goalId);
        when(goal.getName()).thenReturn("Goal García");
        when(goal.getMonthlyTarget()).thenReturn(new BigDecimal("1000.00"));

        when(goalUnitRepository.countByUserId(userId)).thenReturn(1L);
        when(goalUnitRepository.findByUserId(userId, 0, 12)).thenReturn(List.of(goal));
        // El servicio hace UNA consulta IN para toda la página (sin N+1)
        when(goalMemberRepository.findByGoalIdIn(List.of(goalId))).thenReturn(List.of(membership));

        var result = groupGoalService.getGoalsByUserId(userId, 0, 12);

        assertThat(result.goals()).hasSize(1);
        assertThat(result.goals().get(0).name()).isEqualTo("Goal García");
        assertThat(result.goals().get(0).activeMembers()).isEqualTo(1);
        assertThat(result.goals().get(0).isAdmin()).isTrue();

        // Guardarraíl anti-N+1: los métodos por-meta no deben tocarse en el listado
        verify(goalMemberRepository, never()).findByGoalIdAndUserId(any(), any());
        verify(goalMemberRepository, never()).findByGoalId(any());
    }

    @Test
    @DisplayName("Obtener detalle de meta")
    void shouldGetGoalDetail() {
        GoalUnit goal = mock(GoalUnit.class);
        when(goal.getId()).thenReturn(goalId);
        when(goal.getName()).thenReturn("Goal García");
        when(goal.getMonthlyTarget()).thenReturn(new BigDecimal("1000.00"));
        when(goal.getDistributionMode()).thenReturn(DistributionMode.EQUITATIVE);

        GoalMember member = mock(GoalMember.class);
        when(member.getUserId()).thenReturn(userId);
        when(member.isActive()).thenReturn(true);
        when(member.isAdmin()).thenReturn(true);
        when(member.getId()).thenReturn(UUID.randomUUID());
        when(member.getRole()).thenReturn(GoalRole.ADMIN);
        when(member.getSalary()).thenReturn(BigDecimal.ZERO);
        when(member.getCustomPercentage()).thenReturn(null);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("ana");

        when(goalUnitRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalMemberRepository.findByGoalId(goalId)).thenReturn(List.of(member));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contributionRepository.findByGoalId(goalId)).thenReturn(List.of());

        var result = groupGoalService.getGoalDetail(goalId, userId);

        assertThat(result.name()).isEqualTo("Goal García");
        assertThat(result.isAdmin()).isTrue();
        assertThat(result.members()).hasSize(1);
    }

    @Test
    @DisplayName("Actualizar miembro")
    void shouldUpdateMember() {
        UUID memberId = UUID.randomUUID();
        GoalMember requester = mock(GoalMember.class);
        when(requester.isAdmin()).thenReturn(true);
        GoalMember member = mock(GoalMember.class);
        when(member.getGoalId()).thenReturn(goalId);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(requester));
        when(goalMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

        UpdateMemberRequestDTO request = new UpdateMemberRequestDTO();
        request.setRole(GoalRole.ADMIN);
        request.setSalary(new BigDecimal("2000.00"));

        groupGoalService.updateMember(goalId, memberId, userId, request);

        verify(member).setRole(GoalRole.ADMIN);
        verify(member).setSalary(new BigDecimal("2000.00"));
        verify(goalMemberRepository).save(member);
    }

    @Test
    @DisplayName("No permite degradar al último administrador")
    void shouldThrowWhenDemotingLastAdmin() {
        UUID memberId = UUID.randomUUID();
        GoalMember requester = mock(GoalMember.class);
        when(requester.isAdmin()).thenReturn(true);
        GoalMember member = mock(GoalMember.class);
        when(member.getGoalId()).thenReturn(goalId);
        when(member.isAdmin()).thenReturn(true);
        when(member.isActive()).thenReturn(true);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(requester));
        when(goalMemberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(goalMemberRepository.findByGoalId(goalId)).thenReturn(List.of(member));

        UpdateMemberRequestDTO request = new UpdateMemberRequestDTO();
        request.setRole(GoalRole.MEMBER);

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> groupGoalService.updateMember(goalId, memberId, userId, request))
                .withMessage("No puedes dejar la meta grupal sin administradores.");
    }

    @Test
    @DisplayName("Permite degradar a un admin si hay más de uno")
    void shouldAllowDemotingAdminWhenMultipleAdmins() {
        UUID memberId = UUID.randomUUID();
        GoalMember requester = mock(GoalMember.class);
        when(requester.isAdmin()).thenReturn(true);
        GoalMember member = mock(GoalMember.class);
        when(member.getGoalId()).thenReturn(goalId);
        when(member.isAdmin()).thenReturn(true);
        when(member.isActive()).thenReturn(true);
        GoalMember otherAdmin = mock(GoalMember.class);
        when(otherAdmin.isAdmin()).thenReturn(true);
        when(otherAdmin.isActive()).thenReturn(true);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(requester));
        when(goalMemberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(goalMemberRepository.findByGoalId(goalId)).thenReturn(List.of(member, otherAdmin));

        UpdateMemberRequestDTO request = new UpdateMemberRequestDTO();
        request.setRole(GoalRole.MEMBER);

        groupGoalService.updateMember(goalId, memberId, userId, request);

        verify(member).setRole(GoalRole.MEMBER);
        verify(goalMemberRepository).save(member);
    }

    @Test
    @DisplayName("Actualizar meta")
    void shouldUpdateGoal() {
        GoalMember requester = mock(GoalMember.class);
        when(requester.isAdmin()).thenReturn(true);
        GoalUnit goal = mock(GoalUnit.class);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(requester));
        when(goalUnitRepository.findById(goalId)).thenReturn(Optional.of(goal));

        UpdateGoalRequestDTO request = new UpdateGoalRequestDTO();
        request.setMonthlyTarget(new BigDecimal("2000.00"));
        request.setDistributionMode(DistributionMode.PROPORTIONAL);

        groupGoalService.updateGoal(goalId, userId, request);

        verify(goal).setMonthlyTarget(new BigDecimal("2000.00"));
        verify(goal).setDistributionMode(DistributionMode.PROPORTIONAL);
        verify(goalUnitRepository).save(goal);
    }

    @Test
    @DisplayName("Eliminar miembro")
    void shouldDeleteMember() {
        UUID memberId = UUID.randomUUID();
        GoalMember requester = mock(GoalMember.class);
        when(requester.isAdmin()).thenReturn(true);
        GoalMember member = mock(GoalMember.class);
        when(member.getGoalId()).thenReturn(goalId);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(requester));
        when(goalMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

        groupGoalService.deleteMember(goalId, memberId, userId);

        verify(member).setActive(false);
        verify(goalMemberRepository).save(member);
    }

    @Test
    @DisplayName("No permite eliminar al último administrador")
    void shouldThrowWhenDeletingLastAdmin() {
        UUID memberId = UUID.randomUUID();
        GoalMember requester = mock(GoalMember.class);
        when(requester.isAdmin()).thenReturn(true);
        GoalMember member = mock(GoalMember.class);
        when(member.getGoalId()).thenReturn(goalId);
        when(member.isAdmin()).thenReturn(true);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(requester));
        when(goalMemberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(goalMemberRepository.findByGoalId(goalId)).thenReturn(List.of(member));

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> groupGoalService.deleteMember(goalId, memberId, userId))
                .withMessage("No puedes dejar la meta grupal sin administradores.");
    }

    @Test
    @DisplayName("Eliminar meta")
    void shouldDeleteGoal() {
        GoalMember requester = mock(GoalMember.class);
        when(requester.isAdmin()).thenReturn(true);

        GoalUnit goal = mock(GoalUnit.class);
        when(goal.isActive()).thenReturn(true);

        GoalMember member = mock(GoalMember.class);
        when(member.isActive()).thenReturn(true);

        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(requester));
        when(goalUnitRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(goalMemberRepository.findByGoalId(goalId)).thenReturn(List.of(member));

        groupGoalService.deleteGoalUnit(goalId, userId);

        verify(goal).setActive(false);
        verify(goalUnitRepository).save(goal);
        verify(goalMemberRepository).save(member);
    }

    @Test
    @DisplayName("Actualizar meta sin ser admin lanza error")
    void shouldThrowWhenNotAdminUpdatingGoal() {
        GoalMember member = mock(GoalMember.class);
        when(member.isAdmin()).thenReturn(false);
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(member));

        UpdateGoalRequestDTO request = new UpdateGoalRequestDTO();

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> groupGoalService.updateGoal(goalId, userId, request))
                .withMessage("Only administrators can edit the goal");
    }

    @Test
    @DisplayName("Eliminar miembro sin ser admin lanza error")
    void shouldThrowWhenNotAdminDeletingMember() {
        UUID memberId = UUID.randomUUID();
        GoalMember member = mock(GoalMember.class);
        when(member.isAdmin()).thenReturn(false);
        when(goalMemberRepository.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(member));

        assertThatExceptionOfType(ForbiddenOperationException.class)
                .isThrownBy(() -> groupGoalService.deleteMember(goalId, memberId, userId))
                .withMessage("Only administrators can delete members");
    }
}