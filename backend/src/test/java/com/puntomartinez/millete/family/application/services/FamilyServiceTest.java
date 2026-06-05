package com.puntomartinez.millete.family.application.services;

import com.puntomartinez.millete.family.domain.model.*;
import com.puntomartinez.millete.family.domain.ports.out.*;
import com.puntomartinez.millete.family.infrastructure.in.controller.dto.AddContributionRequestDTO;
import com.puntomartinez.millete.family.infrastructure.in.controller.dto.UpdateFamilyRequestDTO;
import com.puntomartinez.millete.family.infrastructure.in.controller.dto.UpdateMemberRequestDTO;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;
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
@DisplayName("FamilyService - Servicio de familias")
class FamilyServiceTest {

    @Mock
    private FamilyUnitRepository familyUnitRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private FamilyInvitationRepository familyInvitationRepository;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FamilyContributionRepository contributionRepository;

    @InjectMocks
    private FamilyService familyService;

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Crear unidad familiar")
    void shouldCreateFamilyUnit() {
        when(familyUnitRepository.save(any(FamilyUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(familyMemberRepository.save(any(FamilyMember.class))).thenAnswer(inv -> inv.getArgument(0));

        FamilyUnit result = familyService.createFamilyUnit(userId, "Familia García", new BigDecimal("1000.00"), "EQUITATIVE");

        assertThat(result.getName()).isEqualTo("Familia García");
        assertThat(result.getDistributionMode()).isEqualTo(DistributionMode.EQUITATIVE);
        assertThat(result.isActive()).isTrue();
        verify(familyUnitRepository).save(any(FamilyUnit.class));
        verify(familyMemberRepository).save(any(FamilyMember.class));
    }

    @Test
    @DisplayName("Invitar miembro")
    void shouldInviteMember() {
        UUID familyId = UUID.randomUUID();
        FamilyMember admin = mock(FamilyMember.class);
        lenient().when(admin.isAdmin()).thenReturn(true);
        User invitedUser = mock(User.class);
        lenient().when(invitedUser.getId()).thenReturn(UUID.randomUUID());

        lenient().when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.of(admin));
        lenient().when(userRepository.findByEmail("invitado@mail.com")).thenReturn(Optional.of(invitedUser));
        lenient().when(familyMemberRepository.findByFamilyId(familyId)).thenReturn(List.of());
        lenient().when(familyInvitationRepository.findByFamilyIdAndEmailAndStatus(eq(familyId), eq("invitado@mail.com"), any()))
                .thenReturn(Optional.empty());
        lenient().when(familyInvitationRepository.save(any(FamilyInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        FamilyInvitation result = familyService.inviteMember(userId, familyId, "invitado@mail.com");

        assertThat(result.getEmail()).isEqualTo("invitado@mail.com");
        assertThat(result.getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    @DisplayName("Invitar sin ser admin lanza error")
    void shouldThrowWhenNotAdminInviting() {
        UUID familyId = UUID.randomUUID();
        FamilyMember member = mock(FamilyMember.class);
        when(member.isAdmin()).thenReturn(false);
        when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.of(member));

        assertThatRuntimeException()
                .isThrownBy(() -> familyService.inviteMember(userId, familyId, "invitado@mail.com"))
                .withMessage("Only Admins can invite new members");
    }

    @Test
    @DisplayName("Aceptar invitación")
    void shouldAcceptInvitation() {
        UUID familyId = UUID.randomUUID();
        FamilyInvitation invitation = mock(FamilyInvitation.class);
        when(invitation.isAcceptable()).thenReturn(true);
        when(invitation.getFamilyId()).thenReturn(familyId);

        when(familyInvitationRepository.findByToken("token123")).thenReturn(Optional.of(invitation));
        when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.empty());
        when(familyMemberRepository.save(any(FamilyMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(familyInvitationRepository.save(any(FamilyInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        FamilyInvitation result = familyService.acceptInvitation(userId, "token123");

        verify(invitation).markAsAccepted();
        verify(familyMemberRepository).save(any(FamilyMember.class));
    }

    @Test
    @DisplayName("Aceptar invitación expirada lanza error")
    void shouldThrowWhenInvitationExpired() {
        FamilyInvitation invitation = mock(FamilyInvitation.class);
        when(invitation.isAcceptable()).thenReturn(false);
        when(familyInvitationRepository.findByToken("token123")).thenReturn(Optional.of(invitation));

        assertThatRuntimeException()
                .isThrownBy(() -> familyService.acceptInvitation(userId, "token123"))
                .withMessage("The invitation is not valid or has expired");
    }

    @Test
    @DisplayName("Añadir aportación")
    void shouldAddContribution() {
        UUID familyId = UUID.randomUUID();
        AddContributionRequestDTO request = new AddContributionRequestDTO();
        request.setAmount(new BigDecimal("100.00"));
        when(contributionRepository.save(any(FamilyContribution.class))).thenAnswer(inv -> inv.getArgument(0));

        familyService.addContribution(familyId, userId, request);

        verify(contributionRepository).save(any(FamilyContribution.class));
    }

    @Test
    @DisplayName("Calcular contribuciones de familia")
    void shouldCalculateContributions() {
        UUID familyId = UUID.randomUUID();
        FamilyUnit family = mock(FamilyUnit.class);
        FamilyMember m1 = mock(FamilyMember.class);
        when(familyUnitRepository.findById(familyId)).thenReturn(Optional.of(family));
        when(familyMemberRepository.findByFamilyId(familyId)).thenReturn(List.of(m1));

        familyService.calculateContributions(familyId);

        verify(family).setMembers(List.of(m1));
        verify(family).calculateContributions();
    }

    @Test
    @DisplayName("Obtener familias por usuario")
    void shouldGetFamiliesByUserId() {
        UUID familyId = UUID.randomUUID();
        FamilyMember membership = mock(FamilyMember.class);
        when(membership.getFamilyId()).thenReturn(familyId);
        when(membership.isAdmin()).thenReturn(true);

        FamilyUnit family = mock(FamilyUnit.class);
        when(family.getId()).thenReturn(familyId);
        when(family.getName()).thenReturn("Familia García");
        when(family.getMonthlyTarget()).thenReturn(new BigDecimal("1000.00"));
        when(family.isActive()).thenReturn(true);

        when(familyMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));
        when(familyUnitRepository.findById(familyId)).thenReturn(Optional.of(family));
        when(familyMemberRepository.findByFamilyId(familyId)).thenReturn(List.of(membership));

        var result = familyService.getFamiliesByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Familia García");
        assertThat(result.get(0).isAdmin()).isTrue();
    }

    @Test
    @DisplayName("Obtener detalle de familia")
    void shouldGetFamilyDetail() {
        UUID familyId = UUID.randomUUID();
        FamilyUnit family = mock(FamilyUnit.class);
        when(family.getId()).thenReturn(familyId);
        when(family.getName()).thenReturn("Familia García");
        when(family.getMonthlyTarget()).thenReturn(new BigDecimal("1000.00"));
        when(family.getDistributionMode()).thenReturn(DistributionMode.EQUITATIVE);

        FamilyMember member = mock(FamilyMember.class);
        when(member.getUserId()).thenReturn(userId);
        when(member.isActive()).thenReturn(true);
        when(member.isAdmin()).thenReturn(true);
        when(member.getId()).thenReturn(UUID.randomUUID());
        when(member.getRole()).thenReturn(FamilyRole.ADMIN);
        when(member.getSalary()).thenReturn(BigDecimal.ZERO);
        when(member.getCustomPercentage()).thenReturn(null);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("ana");

        when(familyUnitRepository.findById(familyId)).thenReturn(Optional.of(family));
        when(familyMemberRepository.findByFamilyId(familyId)).thenReturn(List.of(member));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contributionRepository.findByFamilyId(familyId)).thenReturn(List.of());

        var result = familyService.getFamilyDetail(familyId, userId);

        assertThat(result.name()).isEqualTo("Familia García");
        assertThat(result.isAdmin()).isTrue();
        assertThat(result.members()).hasSize(1);
    }

    @Test
    @DisplayName("Actualizar miembro")
    void shouldUpdateMember() {
        UUID familyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        FamilyMember requester = mock(FamilyMember.class);
        when(requester.isAdmin()).thenReturn(true);
        FamilyMember member = mock(FamilyMember.class);

        when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.of(requester));
        when(familyMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

        UpdateMemberRequestDTO request = new UpdateMemberRequestDTO();
        request.setRole("ADMIN");
        request.setSalary(new BigDecimal("2000.00"));

        familyService.updateMember(familyId, memberId, userId, request);

        verify(member).setRole(FamilyRole.ADMIN);
        verify(member).setSalary(new BigDecimal("2000.00"));
        verify(familyMemberRepository).save(member);
    }

    @Test
    @DisplayName("Actualizar familia")
    void shouldUpdateFamily() {
        UUID familyId = UUID.randomUUID();
        FamilyMember requester = mock(FamilyMember.class);
        when(requester.isAdmin()).thenReturn(true);
        FamilyUnit family = mock(FamilyUnit.class);

        when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.of(requester));
        when(familyUnitRepository.findById(familyId)).thenReturn(Optional.of(family));

        UpdateFamilyRequestDTO request = new UpdateFamilyRequestDTO();
        request.setMonthlyTarget(new BigDecimal("2000.00"));
        request.setDistributionMode("PROPORTIONAL");

        familyService.updateFamily(familyId, userId, request);

        verify(family).setMonthlyTarget(new BigDecimal("2000.00"));
        verify(family).setDistributionMode(DistributionMode.PROPORTIONAL);
        verify(familyUnitRepository).save(family);
    }

    @Test
    @DisplayName("Eliminar miembro")
    void shouldDeleteMember() {
        UUID familyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        FamilyMember requester = mock(FamilyMember.class);
        when(requester.isAdmin()).thenReturn(true);
        FamilyMember member = mock(FamilyMember.class);

        when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.of(requester));
        when(familyMemberRepository.findById(memberId)).thenReturn(Optional.of(member));

        familyService.deleteMember(familyId, memberId, userId);

        verify(member).setActive(false);
        verify(familyMemberRepository).save(member);
    }

    @Test
    @DisplayName("Actualizar familia sin ser admin lanza error")
    void shouldThrowWhenNotAdminUpdatingFamily() {
        UUID familyId = UUID.randomUUID();
        FamilyMember member = mock(FamilyMember.class);
        when(member.isAdmin()).thenReturn(false);
        when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.of(member));

        UpdateFamilyRequestDTO request = new UpdateFamilyRequestDTO();

        assertThatRuntimeException()
                .isThrownBy(() -> familyService.updateFamily(familyId, userId, request))
                .withMessage("Only administrators can edit the family");
    }

    @Test
    @DisplayName("Eliminar miembro sin ser admin lanza error")
    void shouldThrowWhenNotAdminDeletingMember() {
        UUID familyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        FamilyMember member = mock(FamilyMember.class);
        when(member.isAdmin()).thenReturn(false);
        when(familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)).thenReturn(Optional.of(member));

        assertThatRuntimeException()
                .isThrownBy(() -> familyService.deleteMember(familyId, memberId, userId))
                .withMessage("Only administrators can delete members");
    }
}