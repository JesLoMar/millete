package com.puntomartinez.millete.groupgoals.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoalUnit {
    private UUID id;
    private String name;
    private BigDecimal monthlyTarget;
    private DistributionMode distributionMode;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;
    private List<GoalMember> members;

    public Map<UUID, BigDecimal> calculateContributions() {
        Map<UUID, BigDecimal> contributions = new HashMap<>();
        if (members == null || members.isEmpty() || monthlyTarget.compareTo(BigDecimal.ZERO) == 0) {
            return contributions;
        }
        switch (distributionMode) {
            case EQUITATIVE -> {
                BigDecimal equalShare = monthlyTarget.divide(new BigDecimal(members.size()), 2, RoundingMode.HALF_UP);
                members.forEach(m -> contributions.put(m.getUserId(), equalShare));
            }
            case PROPORTIONAL -> {
                BigDecimal totalSalary = members.stream().map(GoalMember::getSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalSalary.compareTo(BigDecimal.ZERO) == 0)
                    throw new IllegalStateException("Total salary cannot be zero in PROPORTIONAL mode");
                for (GoalMember m : members) {
                    BigDecimal pct = m.getSalary().divide(totalSalary, 4, RoundingMode.HALF_UP);
                    contributions.put(m.getUserId(), monthlyTarget.multiply(pct).setScale(2, RoundingMode.HALF_UP));
                }
            }
            case CUSTOM -> {
                for (GoalMember m : members) {
                    if (m.getCustomPercentage() != null) {
                        BigDecimal pct = m.getCustomPercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                        contributions.put(m.getUserId(), monthlyTarget.multiply(pct).setScale(2, RoundingMode.HALF_UP));
                    }
                }
            }
        }
        return contributions;
    }
}