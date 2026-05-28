package com.puntomartinez.millete.family.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("FamilyUnit - Cálculo de contribuciones")
class FamilyUnitTest {

    @Test
    @DisplayName("Modo EQUITATIVE divide el objetivo en partes iguales")
    void shouldCalculateEquitative() {
        FamilyUnit unit = createFamily(DistributionMode.EQUITATIVE);
        FamilyMember m1 = createMember(UUID.randomUUID(), FamilyRole.MEMBER, new BigDecimal("1000"));
        FamilyMember m2 = createMember(UUID.randomUUID(), FamilyRole.MEMBER, new BigDecimal("2000"));
        unit.setMembers(List.of(m1, m2));

        Map<UUID, BigDecimal> contributions = unit.calculateContributions();

        assertThat(contributions).hasSize(2);
        assertThat(contributions.get(m1.getUserId())).isEqualByComparingTo("500.00");
        assertThat(contributions.get(m2.getUserId())).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Modo PROPORTIONAL calcula según salario")
    void shouldCalculateProportional() {
        FamilyUnit unit = createFamily(DistributionMode.PROPORTIONAL);
        FamilyMember m1 = createMember(UUID.randomUUID(), FamilyRole.MEMBER, new BigDecimal("1000"));
        FamilyMember m2 = createMember(UUID.randomUUID(), FamilyRole.MEMBER, new BigDecimal("3000"));
        unit.setMembers(List.of(m1, m2));

        Map<UUID, BigDecimal> contributions = unit.calculateContributions();

        assertThat(contributions.get(m1.getUserId())).isEqualByComparingTo("250.00");
        assertThat(contributions.get(m2.getUserId())).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("Modo CUSTOM usa porcentajes asignados")
    void shouldCalculateCustom() {
        FamilyUnit unit = createFamily(DistributionMode.CUSTOM);
        FamilyMember m1 = createMemberWithPercentage(UUID.randomUUID(), FamilyRole.MEMBER, new BigDecimal("30"));
        FamilyMember m2 = createMemberWithPercentage(UUID.randomUUID(), FamilyRole.MEMBER, new BigDecimal("70"));
        unit.setMembers(List.of(m1, m2));

        Map<UUID, BigDecimal> contributions = unit.calculateContributions();

        assertThat(contributions.get(m1.getUserId())).isEqualByComparingTo("300.00");
        assertThat(contributions.get(m2.getUserId())).isEqualByComparingTo("700.00");
    }

    private FamilyUnit createFamily(DistributionMode mode) {
        FamilyUnit unit = new FamilyUnit();
        unit.setMonthlyTarget(new BigDecimal("1000.00"));
        unit.setDistributionMode(mode);
        return unit;
    }

    private FamilyMember createMember(UUID userId, FamilyRole role, BigDecimal salary) {
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setRole(role);
        member.setSalary(salary);
        return member;
    }

    private FamilyMember createMemberWithPercentage(UUID userId, FamilyRole role, BigDecimal percentage) {
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setRole(role);
        member.setCustomPercentage(percentage);
        return member;
    }
}