package com.puntomartinez.millete.family.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class FamilyUnit {
    private UUID id;
    private String name;
    private BigDecimal monthlyTarget;
    private DistributionMode distributionMode;
    private List<FamilyMember> members;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;


    /**
     * BUSINESS LOGIC - STORY 7
     */
    public Map<UUID, BigDecimal> calculateContributions() {
        Map<UUID, BigDecimal> contributions = new HashMap<>();

        if (members == null || members.isEmpty() || monthlyTarget.compareTo(BigDecimal.ZERO) == 0) {
            return contributions;
        }

        switch (distributionMode) {
            case EQUITATIVE:
                BigDecimal equalShare = monthlyTarget.divide(new BigDecimal(members.size()), 2, RoundingMode.HALF_UP);
                members.forEach(member -> contributions.put(member.getUserId(), equalShare));
                break;

            case PROPORTIONAL:
                BigDecimal totalSalary = members.stream()
                        .map(FamilyMember::getSalary)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalSalary.compareTo(BigDecimal.ZERO) == 0) break;

                for (FamilyMember member : members) {
                    BigDecimal percentage = member.getSalary().divide(totalSalary, 4, RoundingMode.HALF_UP);
                    BigDecimal contribution = monthlyTarget.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
                    contributions.put(member.getUserId(), contribution);
                }
                break;

            case CUSTOM:
                for (FamilyMember member : members) {
                    if (member.getCustomPercentage() != null) {
                        BigDecimal percentage = member.getCustomPercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                        BigDecimal contribution = monthlyTarget.multiply(percentage).setScale(2, RoundingMode.HALF_UP);
                        contributions.put(member.getUserId(), contribution);
                    }
                }
                break;
        }

        return contributions;
    }
}