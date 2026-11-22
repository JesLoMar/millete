package com.puntomartinez.millete.dashboard.application.services;

import com.puntomartinez.millete.dashboard.domain.ports.out.TransactionQueryPort;
import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;
import com.puntomartinez.millete.users.application.services.UserLocalDateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardHistoryServiceTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 7);

    @Mock TransactionQueryPort transactions;
    @Mock UserLocalDateService dates;
    @InjectMocks DashboardHistoryService service;

    @ParameterizedTest
    @CsvSource({
        "week, 2026-01-01, 7",
        "month, 2025-12-08, 5",
        "year, 2025-02-01, 12"
    })
    void endsTodayAndShowsOnlyTheRequestedNumberOfBuckets(String period, LocalDate start, int size) {
        when(dates.todayFor(USER_ID)).thenReturn(TODAY);
        when(transactions.findByUserIdAndDateBetween(USER_ID, start, TODAY)).thenReturn(List.of(
                transaction(start, "-10", "EXPENSE"),
                transaction(TODAY, "-20", "EXPENSE"),
                transaction(TODAY, "30", "INCOME"),
                transaction(TODAY.plusDays(1), "999", "INCOME"),
                transaction(start.minusDays(1), "-999", "EXPENSE")
        ));

        var history = service.getHistory(USER_ID, period);

        assertThat(history.period()).isEqualTo(period);
        assertThat(history.labels()).hasSize(size);
        assertThat(history.data()).hasSize(size);
        assertThat(history.data().getFirst()).isEqualByComparingTo("10");
        assertThat(history.data().getLast()).isEqualByComparingTo("20");
        assertThat(history.data().subList(1, size - 1)).containsOnly(BigDecimal.ZERO);
        assertThat(history.points()).hasSize(size);
        assertThat(history.points().getFirst().start()).isEqualTo(start);
        assertThat(history.points().getLast().end()).isEqualTo(TODAY);
        assertThat(history.points().getLast().income()).isEqualByComparingTo("30");
        assertThat(history.points()).allSatisfy(point -> assertThat(point.end()).isBeforeOrEqualTo(TODAY));
    }

    @Test
    void rejectsUnsupportedPeriods() {
        assertThatThrownBy(() -> service.getHistory(USER_ID, "invalid"))
                .isInstanceOf(InvalidInputException.class);
    }

    private TransactionQueryPort.TransactionData transaction(LocalDate date, String amount, String type) {
        return new TransactionQueryPort.TransactionData(
                UUID.randomUUID(), type, null, new BigDecimal(amount), date, type
        );
    }
}
