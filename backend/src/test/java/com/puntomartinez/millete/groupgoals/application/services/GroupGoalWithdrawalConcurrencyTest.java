package com.puntomartinez.millete.groupgoals.application.services;

import com.puntomartinez.millete.groupgoals.domain.model.*;
import com.puntomartinez.millete.groupgoals.domain.ports.in.WithdrawContributionUseCase;
import com.puntomartinez.millete.groupgoals.domain.ports.out.*;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupGoalWithdrawalConcurrencyTest {
    @Test
    void concurrentWithdrawalsCannotBothSpendTheSameBalance() throws Exception {
        UUID goalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GoalUnitRepository goals = mock(GoalUnitRepository.class);
        GoalMemberRepository members = mock(GoalMemberRepository.class);
        GoalContributionRepository contributions = mock(GoalContributionRepository.class);
        GoalInvitationRepository invitations = mock(GoalInvitationRepository.class);
        TimeProvider time = mock(TimeProvider.class);
        when(goals.findById(goalId)).thenReturn(Optional.of(GoalUnit.reconstitute(
                goalId, "Trip", new BigDecimal("100.00"), DistributionMode.EQUITATIVE,
                java.time.Instant.now(), java.time.Instant.now(), true)));
        when(members.findByGoalIdAndUserId(goalId, userId)).thenReturn(Optional.of(
                GoalMember.reconstitute(UUID.randomUUID(), goalId, userId, GoalRole.MEMBER,
                        null, null, java.time.Instant.now(), java.time.Instant.now(),
                        java.time.Instant.now(), true)));
        CountDownLatch lockAttempts = new CountDownLatch(2);
        ReentrantLock memberRow = new ReentrantLock();
        AtomicReference<BigDecimal> withdrawals = new AtomicReference<>(BigDecimal.ZERO);
        when(members.findActiveByGoalIdAndUserIdForUpdate(goalId, userId)).thenAnswer(invocation -> {
            lockAttempts.countDown();
            if (!lockAttempts.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Both requests did not reach the member lock");
            }
            memberRow.lock();
            return members.findByGoalIdAndUserId(goalId, userId);
        });
        when(contributions.sumByGoalIdAndUserId(goalId, userId)).thenAnswer(invocation -> {
            return Optional.of(new GoalContributionRepository.MemberContributionTotals(
                    userId, new BigDecimal("100.00"), withdrawals.get(),
                    new BigDecimal("100.00").subtract(withdrawals.get())));
        });
        when(time.now()).thenReturn(java.time.Instant.now());
        when(contributions.save(any())).thenAnswer(invocation -> {
            GoalContribution contribution = invocation.getArgument(0);
            withdrawals.updateAndGet(current -> current.add(contribution.getAmount()));
            memberRow.unlock();
            return contribution;
        });
        GroupGoalCommandService service = new GroupGoalCommandService(
                goals, members, contributions, invitations, time);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> withdraw = () -> {
                try {
                    service.withdrawContribution(goalId, userId,
                            new WithdrawContributionUseCase.WithdrawContributionCommand(
                                    new BigDecimal("80.00")));
                    return true;
                } catch (RuntimeException exception) {
                    return false;
                }
            };
            Future<Boolean> first = executor.submit(withdraw);
            Future<Boolean> second = executor.submit(withdraw);
            int successful = (first.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);
            assertThat(successful).isEqualTo(1);
            verify(contributions, times(1)).save(any());
        } finally {
            executor.shutdownNow();
        }
    }
}
