package com.puntomartinez.millete.groupgoals.infrastructure.out.persistence.postgresql.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface MemberContributionTotalsProjection {

    UUID getUserId();

    BigDecimal getTotalDeposits();

    BigDecimal getTotalWithdrawals();
}