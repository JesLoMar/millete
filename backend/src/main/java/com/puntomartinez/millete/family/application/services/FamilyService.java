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
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
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

        log.info("Family unit created: {} (ID: {}) by user {}", name, familyUnit.getId(), adminUserId);
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

        User invitedUser = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new RuntimeException("No user found with that email"));

        List<FamilyMember> members = familyMemberRepository.findByFamilyId(familyId);
        boolean alreadyMember = members.stream()
                .anyMatch(m -> m.getUserId().equals(invitedUser.getId()) && m.isActive());
        if (alreadyMember) {
            throw new RuntimeException("User is already a member of this family");
        }

        familyInvitationRepository.findByFamilyIdAndEmailAndStatus(familyId, guestEmail, InvitationStatus.PENDING)
                .ifPresent(i -> {
                    throw new RuntimeException("A pending invitation already exists for this email");
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

        try {
            emailSenderPort.sendInvitationEmail(guestEmail, invitation.getToken());
            log.info("Invitation sent to {} for family {}", guestEmail, familyId);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", guestEmail, e.getMessage(), e);
        }

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
                .orElseThrow(() -> new RuntimeException("Family not found"));

        List<FamilyMember> allMembers = familyMemberRepository.findByFamilyId(familyId);
        boolean isMember = allMembers.stream().anyMatch(m -> m.getUserId().equals(userId) && m.isActive());
        if (!isMember) {
            throw new RuntimeException("You do not have access to this family");
        }

        boolean isAdmin = allMembers.stream()
                .anyMatch(m -> m.getUserId().equals(userId) && m.isAdmin() && m.isActive());

        List<FamilyMemberDTO> memberDTOs = allMembers.stream()
                .filter(FamilyMember::isActive)
                .map(m -> {
                    String memberName = userRepository.findById(m.getUserId())
                            .map(u -> u.getUsername() != null ? u.getUsername() : u.getEmail())
                            .orElse("Member");
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
                            .orElse("Member");
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
                .orElseThrow(() -> new RuntimeException("You are not a member of this family"));
        if (!requester.isAdmin()) {
            throw new RuntimeException("Only administrators can edit members");
        }

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (request.getRole() != null) {
            member.setRole(FamilyRole.valueOf(request.getRole()));
        }

        if (request.getSalary() != null) {
            member.setSalary(request.getSalary());
        }

        if (request.getCustomPercentage() != null) {
            member.setCustomPercentage(request.getCustomPercentage());
        }

        member.setModifiedAt(LocalDateTime.now());
        familyMemberRepository.save(member);
        log.info("Member {} updated in family {} by admin {}", memberId, familyId, userId);
    }

    public void updateFamily(UUID familyId, UUID userId, UpdateFamilyRequestDTO request) {
        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this family"));
        if (!requester.isAdmin()) {
            throw new RuntimeException("Only administrators can edit the family");
        }

        FamilyUnit family = familyUnitRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));

        DistributionMode oldMode = family.getDistributionMode();

        if (request.getMonthlyTarget() != null) {
            family.setMonthlyTarget(request.getMonthlyTarget());
        }

        if (request.getDistributionMode() != null) {
            DistributionMode newMode = DistributionMode.valueOf(request.getDistributionMode());

            if (newMode == DistributionMode.CUSTOM && oldMode != DistributionMode.CUSTOM) {
                List<FamilyMember> members = familyMemberRepository.findByFamilyId(familyId);
                List<FamilyMember> activeMembers = members.stream()
                        .filter(FamilyMember::isActive)
                        .toList();

                List<FamilyMember> membersWithoutPercentage = activeMembers.stream()
                        .filter(m -> m.getCustomPercentage() == null)
                        .toList();

                if (!membersWithoutPercentage.isEmpty()) {
                    int totalMembers = activeMembers.size();
                    BigDecimal equalShare = new BigDecimal("100.00").divide(
                            new BigDecimal(totalMembers), 2, java.math.RoundingMode.HALF_UP);

                    BigDecimal sum = equalShare.multiply(new BigDecimal(membersWithoutPercentage.size() - 1));
                    BigDecimal lastShare = new BigDecimal("100.00").subtract(sum);

                    for (int i = 0; i < membersWithoutPercentage.size(); i++) {
                        FamilyMember m = membersWithoutPercentage.get(i);
                        if (i == membersWithoutPercentage.size() - 1) {
                            m.setCustomPercentage(lastShare);
                        } else {
                            m.setCustomPercentage(equalShare);
                        }
                        m.setModifiedAt(LocalDateTime.now());
                        familyMemberRepository.save(m);
                    }

                    log.info("Auto-assigned customPercentage to {} members in family {} for CUSTOM mode",
                            membersWithoutPercentage.size(), familyId);
                }
            }

            family.setDistributionMode(newMode);
            log.info("Family {} distribution mode changed from {} to {}", familyId, oldMode, newMode);
        }

        family.setModifiedAt(LocalDateTime.now());
        familyUnitRepository.save(family);
    }

    public void deleteMember(UUID familyId, UUID memberId, UUID userId) {
        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this family"));
        if (!requester.isAdmin()) {
            throw new RuntimeException("Only administrators can delete members");
        }

        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setActive(false);
        member.setModifiedAt(LocalDateTime.now());
        familyMemberRepository.save(member);
        log.info("Member {} removed from family {} by admin {}", memberId, familyId, userId);
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
        log.info("Contribution of {} added to family {} by user {}", request.getAmount(), familyId, userId);
    }

    @Override
    @Transactional
    public FamilyInvitation acceptInvitation(UUID userId, String token) {
        FamilyInvitation invitation = familyInvitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.isAcceptable()) {
            throw new RuntimeException("The invitation is not valid or has expired");
        }

        familyMemberRepository.findByFamilyIdAndUserId(invitation.getFamilyId(), userId)
                .ifPresent(m -> {
                    throw new RuntimeException("You are already a member of this family");
                });

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

        invitation.markAsAccepted();
        familyInvitationRepository.save(invitation);
        log.info("User {} accepted invitation to family {}", userId, invitation.getFamilyId());

        return invitation;
    }
}