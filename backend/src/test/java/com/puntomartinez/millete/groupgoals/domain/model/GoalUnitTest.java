package com.puntomartinez.millete.groupgoals.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("FamilyUnit - Cálculo de contribuciones")
class GoalUnitTest {

    @Test
    @DisplayName("Modo EQUITATIVE divide el objetivo en partes iguales")
    void shouldCalculateEquitative() {
        GoalUnit unit = createFamily(DistributionMode.EQUITATIVE);
        GoalMember m1 = createMember(UUID.randomUUID(), GoalRole.MEMBER, new BigDecimal("1000"));
        GoalMember m2 = createMember(UUID.randomUUID(), GoalRole.MEMBER, new BigDecimal("2000"));
        unit.setMembers(List.of(m1, m2));

        Map<UUID, BigDecimal> contributions = unit.calculateContributions();

        assertThat(contributions).hasSize(2);
        assertThat(contributions.get(m1.getUserId())).isEqualByComparingTo("500.00");
        assertThat(contributions.get(m2.getUserId())).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Modo PROPORTIONAL calcula según salario")
    void shouldCalculateProportional() {
        GoalUnit unit = createFamily(DistributionMode.PROPORTIONAL);
        GoalMember m1 = createMember(UUID.randomUUID(), GoalRole.MEMBER, new BigDecimal("1000"));
        GoalMember m2 = createMember(UUID.randomUUID(), GoalRole.MEMBER, new BigDecimal("3000"));
        unit.setMembers(List.of(m1, m2));

        Map<UUID, BigDecimal> contributions = unit.calculateContributions();

        assertThat(contributions.get(m1.getUserId())).isEqualByComparingTo("250.00");
        assertThat(contributions.get(m2.getUserId())).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("Modo CUSTOM usa porcentajes asignados")
    void shouldCalculateCustom() {
        GoalUnit unit = createFamily(DistributionMode.CUSTOM);
        GoalMember m1 = createMemberWithPercentage(UUID.randomUUID(), GoalRole.MEMBER, new BigDecimal("30"));
        GoalMember m2 = createMemberWithPercentage(UUID.randomUUID(), GoalRole.MEMBER, new BigDecimal("70"));
        unit.setMembers(List.of(m1, m2));

        Map<UUID, BigDecimal> contributions = unit.calculateContributions();

        assertThat(contributions.get(m1.getUserId())).isEqualByComparingTo("300.00");
        assertThat(contributions.get(m2.getUserId())).isEqualByComparingTo("700.00");
    }

    private GoalUnit createFamily(DistributionMode mode) {
        GoalUnit unit = new GoalUnit();
        unit.setMonthlyTarget(new BigDecimal("1000.00"));
        unit.setDistributionMode(mode);
        return unit;
    }

    private GoalMember createMember(UUID userId, GoalRole role, BigDecimal salary) {
        GoalMember member = new GoalMember();
        member.setUserId(userId);
        member.setRole(role);
        member.setSalary(salary);
        return member;
    }

    private GoalMember createMemberWithPercentage(UUID userId, GoalRole role, BigDecimal percentage) {
        GoalMember member = new GoalMember();
        member.setUserId(userId);
        member.setRole(role);
        member.setCustomPercentage(percentage);
        return member;
    }
}
