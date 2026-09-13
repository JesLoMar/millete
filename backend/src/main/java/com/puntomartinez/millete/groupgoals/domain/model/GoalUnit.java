package com.puntomartinez.millete.groupgoals.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GoalUnit {

    private final UUID id;
    private String name;
    private BigDecimal monthlyTarget;
    private DistributionMode distributionMode;
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean active;

    private GoalUnit(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active) {

        this.id = requireId(id);
        this.name = requireName(name);
        this.monthlyTarget = monthlyTarget;
        this.distributionMode = requireDistributionMode(distributionMode);
        this.createdAt = requireDate(
                createdAt,
                "La fecha de creación es obligatoria."
        );
        this.modifiedAt = requireDate(
                modifiedAt,
                "La fecha de modificación es obligatoria."
        );
        this.active = active;
    }

    public static GoalUnit create(
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode) {

        LocalDateTime now = LocalDateTime.now();

        return new GoalUnit(
                UUID.randomUUID(),
                name,
                monthlyTarget,
                distributionMode,
                now,
                now,
                true
        );
    }

    public static GoalUnit reconstitute(
            UUID id,
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            boolean active) {

        return new GoalUnit(
                id,
                name,
                monthlyTarget,
                distributionMode,
                createdAt,
                modifiedAt,
                active
        );
    }

    public void updateDetails(
            String name,
            BigDecimal monthlyTarget,
            DistributionMode distributionMode) {

        if (name != null) {
            this.name = requireName(name);
        }

        if (monthlyTarget != null) {
            this.monthlyTarget = monthlyTarget;
        }

        if (distributionMode != null) {
            this.distributionMode = requireDistributionMode(distributionMode);
        }

        this.modifiedAt = LocalDateTime.now();
    }

    public Map<UUID, BigDecimal> calculateContributions(
            List<GoalMember> members) {

        Map<UUID, BigDecimal> contributions = new HashMap<>();

        if (members == null
                || members.isEmpty()
                || monthlyTarget == null
                || monthlyTarget.compareTo(BigDecimal.ZERO) == 0) {

            return contributions;
        }

        switch (distributionMode) {
            case EQUITATIVE -> {
                BigDecimal equalShare = monthlyTarget.divide(
                        new BigDecimal(members.size()),
                        2,
                        RoundingMode.HALF_UP
                );

                members.forEach(member ->
                        contributions.put(
                                member.getUserId(),
                                equalShare
                        )
                );
            }

            case PROPORTIONAL -> {
                BigDecimal totalSalary = members.stream()
                        .map(GoalMember::getSalary)
                        .filter(java.util.Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalSalary.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalStateException(
                            "Total salary cannot be zero in PROPORTIONAL mode"
                    );
                }

                for (GoalMember member : members) {
                    BigDecimal salary = member.getSalary();

                    if (salary == null) {
                        salary = BigDecimal.ZERO;
                    }

                    BigDecimal percentage = salary.divide(
                            totalSalary,
                            4,
                            RoundingMode.HALF_UP
                    );

                    contributions.put(
                            member.getUserId(),
                            monthlyTarget
                                    .multiply(percentage)
                                    .setScale(2, RoundingMode.HALF_UP)
                    );
                }
            }

            case CUSTOM -> {
    BigDecimal totalPercentage = members.stream()
            .map(GoalMember::getCustomPercentage)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    boolean allMembersHavePercentage = members.stream()
            .allMatch(member -> member.getCustomPercentage() != null);

    if (!allMembersHavePercentage) {
        throw new IllegalStateException(
                "Todos los miembros deben tener un porcentaje personalizado."
        );
    }

    if (totalPercentage.compareTo(new BigDecimal("100")) != 0) {
        throw new IllegalStateException(
                "Los porcentajes personalizados deben sumar 100%."
        );
    }

    for (GoalMember member : members) {
        BigDecimal percentage = member.getCustomPercentage()
                .divide(
                        new BigDecimal("100"),
                        4,
                        RoundingMode.HALF_UP
                );

        contributions.put(
                member.getUserId(),
                monthlyTarget
                        .multiply(percentage)
                        .setScale(2, RoundingMode.HALF_UP)
        );
    }
}
        }

        return contributions;
    }

    public void deactivate() {
        this.active = false;
        this.modifiedAt = LocalDateTime.now();
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El id de la meta es obligatorio."
            );
        }
        return id;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la meta es obligatorio."
            );
        }
        return name.trim();
    }

    private static DistributionMode requireDistributionMode(
            DistributionMode distributionMode) {

        if (distributionMode == null) {
            throw new IllegalArgumentException(
                    "El modo de distribución es obligatorio."
            );
        }

        return distributionMode;
    }

    private static LocalDateTime requireDate(
            LocalDateTime value,
            String message) {

        if (value == null) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getMonthlyTarget() {
        return monthlyTarget;
    }

    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public boolean isActive() {
        return active;
    }
}