package com.puntomartinez.millete.family.application.services;

import com.puntomartinez.millete.family.domain.model.*;
import com.puntomartinez.millete.family.domain.ports.in.AcceptInvitationUseCase;
import com.puntomartinez.millete.family.domain.ports.in.CalculateFamilyContributionsUseCase;
import com.puntomartinez.millete.family.domain.ports.in.CreateFamilyUnitUseCase;
import com.puntomartinez.millete.family.domain.ports.in.InviteFamilyMemberUseCase;
import com.puntomartinez.millete.family.domain.ports.out.*;
import com.puntomartinez.millete.family.infrastructure.in.controller.dto.*;
import com.puntomartinez.millete.users.domain.model.User;
import com.puntomartinez.millete.users.domain.ports.out.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FamilyService implements
        CreateFamilyUnitUseCase,
        CalculateFamilyContributionsUseCase,
        InviteFamilyMemberUseCase,
        AcceptInvitationUseCase {

    private final FamilyUnitRepository familyUnitRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyInvitationRepository familyInvitationRepository;
    private final EmailSenderPort emailSenderPort;
    private final UserRepository userRepository;
    private final FamilyContributionRepository contributionRepository;

    public FamilyService(
            FamilyUnitRepository familyUnitRepository,
            FamilyMemberRepository familyMemberRepository,
            FamilyInvitationRepository familyInvitationRepository,
            EmailSenderPort emailSenderPort,
            UserRepository userRepository,
            FamilyContributionRepository contributionRepository) {
        this.familyUnitRepository = familyUnitRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.familyInvitationRepository = familyInvitationRepository;
        this.emailSenderPort = emailSenderPort;
        this.userRepository = userRepository;
        this.contributionRepository = contributionRepository;
    }

    @Override
    @Transactional
    public FamilyUnit createFamilyUnit(UUID adminUserId, String name, BigDecimal monthlyTarget, String distributionMode) {
        FamilyUnit familyUnit = new FamilyUnit();
        familyUnit.setId(UUID.randomUUID());
        familyUnit.setName(name);
        familyUnit.setMonthlyTarget(monthlyTarget);
        familyUnit.setDistributionMode(DistributionMode.valueOf(distributionMode.toUpperCase()));
        familyUnit.setCreatedAt(LocalDateTime.now());
        familyUnit.setModifiedAt(LocalDateTime.now());
        familyUnit.setActive(true);

        familyUnit = familyUnitRepository.save(familyUnit);

        FamilyMember adminMember = new FamilyMember();
        adminMember.setId(UUID.randomUUID());
        adminMember.setFamilyId(familyUnit.getId());
        adminMember.setUserId(adminUserId);
        adminMember.setRole(FamilyRole.ADMIN);
        adminMember.setSalary(BigDecimal.ZERO);
        adminMember.setJoinedAt(LocalDateTime.now());
        adminMember.setCreatedAt(LocalDateTime.now());
        adminMember.setModifiedAt(LocalDateTime.now());
        adminMember.setActive(true);

        familyMemberRepository.save(adminMember);

        return familyUnit;
    }

    @Override
    public Map<UUID, BigDecimal> calculateContributions(UUID familyId) {
        FamilyUnit familyUnit = familyUnitRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family Unit not found"));
        List<FamilyMember> members = familyMemberRepository.findByFamilyId(familyId);
        familyUnit.setMembers(members);
        return familyUnit.calculateContributions();
    }

    @Override
    @Transactional
    public FamilyInvitation inviteMember(UUID adminUserId, UUID familyId, String guestEmail) {
        FamilyMember inviter = familyMemberRepository.findByFamilyIdAndUserId(familyId, adminUserId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this family"));

        if (!inviter.isAdmin()) {
            throw new RuntimeException("Only Admins can invite new members");
        }

        // Verificar que el email pertenece a un usuario registrado
        User invitedUser = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese email"));

        // Verificar que no sea ya miembro activo
        List<FamilyMember> members = familyMemberRepository.findByFamilyId(familyId);
        boolean alreadyMember = members.stream()
                .anyMatch(m -> m.getUserId().equals(invitedUser.getId()) && m.isActive());
        if (alreadyMember) {
            throw new RuntimeException("El usuario ya es miembro de esta familia");
        }

        // Verificar que no tenga ya invitación PENDING
        familyInvitationRepository.findByFamilyIdAndEmailAndStatus(familyId, guestEmail, InvitationStatus.PENDING)
                .ifPresent(i -> {
                    throw new RuntimeException("Ya existe una invitación pendiente para este email");
                });

        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setId(UUID.randomUUID());
        invitation.setFamilyId(familyId);
        invitation.setEmail(guestEmail);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setModifiedAt(LocalDateTime.now());
        invitation.setActive(true);

        invitation = familyInvitationRepository.save(invitation);
        emailSenderPort.sendInvitationEmail(guestEmail, invitation.getToken());

        return invitation;
    }

    public List<FamilyListItemResponseDTO> getFamiliesByUserId(UUID userId) {
        List<FamilyMember> memberships = familyMemberRepository.findByUserId(userId);
        List<FamilyListItemResponseDTO> result = new ArrayList<>();

        for (FamilyMember membership : memberships) {
            FamilyUnit family = familyUnitRepository.findById(membership.getFamilyId())
                    .orElse(null);
            if (family == null || !family.isActive()) continue;

            List<FamilyMember> allMembers = familyMemberRepository.findByFamilyId(family.getId());
            long activeMembers = allMembers.stream().filter(FamilyMember::isActive).count();

            result.add(new FamilyListItemResponseDTO(
                    family.getId(),
                    family.getName(),
                    family.getMonthlyTarget(),
                    (int) activeMembers,
                    membership.isAdmin()
            ));
        }

        result.sort((a, b) -> {
            if (a.isAdmin() && !b.isAdmin()) return -1;
            if (!a.isAdmin() && b.isAdmin()) return 1;
            return a.name().compareTo(b.name());
        });

        return result;
    }

    public FamilyDetailResponseDTO getFamilyDetail(UUID familyId, UUID userId) {
        FamilyUnit family = familyUnitRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Familia no encontrada"));

        List<FamilyMember> allMembers = familyMemberRepository.findByFamilyId(familyId);
        boolean isMember = allMembers.stream().anyMatch(m -> m.getUserId().equals(userId) && m.isActive());
        if (!isMember) {
            throw new RuntimeException("No tienes acceso a esta familia");
        }

        boolean isAdmin = allMembers.stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isAdmin() && m.isActive());

        List<FamilyMemberDTO> memberDTOs = allMembers.stream()
                .filter(FamilyMember::isActive)
                .map(m -> {
                    String memberName = userRepository.findById(m.getUserId())
                            .map(u -> u.getUsername() != null ? u.getUsername() : u.getEmail())
                            .orElse("Miembro");
                    return new FamilyMemberDTO(
                            m.getId(),
                            m.getUserId(),
                            memberName,
                            m.getRole().name(),
                            m.getSalary(),
                            m.getCustomPercentage()
                    );
                })
                .toList();

        List<FamilyContribution> contributions = contributionRepository.findByFamilyId(familyId);
        List<FamilyContributionDTO> contributionDTOs = contributions.stream()
                .map(c -> {
                    String userName = userRepository.findById(c.getUserId())
                            .map(u -> u.getUsername() != null ? u.getUsername() : u.getEmail())
                            .orElse("Miembro");
                    return new FamilyContributionDTO(
                            c.getId(),
                            c.getUserId(),
                            userName,
                            c.getAmount(),
                            c.getDate()
                    );
                })
                .toList();

        return new FamilyDetailResponseDTO(
                family.getId(),
                family.getName(),
                family.getMonthlyTarget(),
                family.getDistributionMode().name(),
                isAdmin,
                memberDTOs,
                contributionDTOs
        );
    }

    public void updateMember(UUID familyId, UUID memberId, UUID userId, UpdateMemberRequestDTO request) {
        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de esta familia"));
        if (!requester.isAdmin()) {
            throw new RuntimeException("Solo los administradores pueden editar miembros");
        }

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado"));

        if (request.getRole() != null) member.setRole(FamilyRole.valueOf(request.getRole()));
        if (request.getSalary() != null) member.setSalary(request.getSalary());
        if (request.getCustomPercentage() != null) member.setCustomPercentage(request.getCustomPercentage());
        member.setModifiedAt(LocalDateTime.now());

        familyMemberRepository.save(member);
    }

    public void updateFamily(UUID familyId, UUID userId, UpdateFamilyRequestDTO request) {
        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de esta familia"));
        if (!requester.isAdmin()) {
            throw new RuntimeException("Solo los administradores pueden editar la familia");
        }

        FamilyUnit family = familyUnitRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Familia no encontrada"));

        if (request.getMonthlyTarget() != null) family.setMonthlyTarget(request.getMonthlyTarget());
        if (request.getDistributionMode() != null) family.setDistributionMode(DistributionMode.valueOf(request.getDistributionMode()));
        family.setModifiedAt(LocalDateTime.now());

        familyUnitRepository.save(family);
    }

    public void deleteMember(UUID familyId, UUID memberId, UUID userId) {
        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de esta familia"));
        if (!requester.isAdmin()) {
            throw new RuntimeException("Solo los administradores pueden eliminar miembros");
        }

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado"));

        member.setActive(false);
        member.setModifiedAt(LocalDateTime.now());
        familyMemberRepository.save(member);
    }

    public void addContribution(UUID familyId, UUID userId, AddContributionRequestDTO request) {
        FamilyContribution contribution = new FamilyContribution();
        contribution.setId(UUID.randomUUID());
        contribution.setFamilyId(familyId);
        contribution.setUserId(userId);
        contribution.setAmount(request.getAmount());
        contribution.setDate(LocalDateTime.now());
        contribution.setCreatedAt(LocalDateTime.now());
        contribution.setModifiedAt(LocalDateTime.now());
        contribution.setActive(true);

        contributionRepository.save(contribution);
    }

    @Override
    @Transactional
    public FamilyInvitation acceptInvitation(UUID userId, String token) {
        FamilyInvitation invitation = familyInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));

        if (!invitation.isAcceptable()) {
            throw new RuntimeException("La invitación no es válida o ha expirado");
        }

        // Verificar que el usuario no sea ya miembro
        familyMemberRepository.findByFamilyIdAndUserId(invitation.getFamilyId(), userId)
                .ifPresent(m -> {
                    throw new RuntimeException("Ya eres miembro de esta familia");
                });

        // Añadir miembro
        FamilyMember member = new FamilyMember();
        member.setId(UUID.randomUUID());
        member.setFamilyId(invitation.getFamilyId());
        member.setUserId(userId);
        member.setRole(FamilyRole.MEMBER);
        member.setSalary(BigDecimal.ZERO);
        member.setJoinedAt(LocalDateTime.now());
        member.setCreatedAt(LocalDateTime.now());
        member.setModifiedAt(LocalDateTime.now());
        member.setActive(true);
        familyMemberRepository.save(member);

        // Marcar invitación aceptada
        invitation.markAsAccepted();
        familyInvitationRepository.save(invitation);

        return invitation;
    }
}