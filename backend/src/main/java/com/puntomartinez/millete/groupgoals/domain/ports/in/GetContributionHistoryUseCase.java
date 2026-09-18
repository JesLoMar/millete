package com.puntomartinez.millete.groupgoals.domain.ports.in;

import com.puntomartinez.millete.groupgoals.domain.model.GoalContribution;
import com.puntomartinez.millete.groupgoals.domain.ports.out.GoalContributionRepository.MemberContributionTotals;

import java.util.List;
import java.util.UUID;

public interface GetContributionHistoryUseCase {

    PaginatedContributions getContributionHistory(
            UUID goalId,
            UUID userId,
            int page,
            int size
    );

    List<MemberContributionTotals> getTotalsByMember(UUID goalId, UUID userId);

    record PaginatedContributions(
            List<GoalContribution> contributions,
            int currentPage,
            int totalPages,
            long totalElements,
            int size,
            boolean first,
            boolean last
    ) {
    }
}