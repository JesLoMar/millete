package com.puntomartinez.millete.investments.application.services;

import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentDistributionItemDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentDistributionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentEvolutionResponseDTO;
import com.puntomartinez.millete.investments.infrastructure.in.controller.dto.InvestmentMetricsResponseDTO;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentDistributionUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentEvolutionUseCase;
import com.puntomartinez.millete.investments.domain.ports.in.GetInvestmentMetricsUseCase;
import com.puntomartinez.millete.investments.domain.ports.out.InvestmentQueryPort;
import com.puntomartinez.millete.shared.domain.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvestmentQueryService implements
        GetInvestmentMetricsUseCase,
        GetInvestmentEvolutionUseCase,
        GetInvestmentDistributionUseCase {

    private static final String[] CHART_COLORS = {
            "hsl(var(--chart-1))",
            "hsl(var(--chart-2))",
            "hsl(var(--chart-3))",
            "hsl(var(--chart-4))",
            "hsl(var(--chart-5))"
    };

    private final InvestmentQueryPort investmentQueryPort;
    private final TimeProvider timeProvider;

    public InvestmentQueryService(
            InvestmentQueryPort investmentQueryPort,
            TimeProvider timeProvider
    ) {
        this.investmentQueryPort = investmentQueryPort;
        this.timeProvider = timeProvider;
    }

    @Override
    public InvestmentMetricsResponseDTO getInvestmentMetrics(
            UUID userId,
            String period
    ) {
        List<InvestmentQueryPort.InvestmentData> investments =
                investmentQueryPort.findAllByUserId(userId);

        LocalDate[] currentRange = getDateRange(period);
        LocalDate[] previousRange = getPreviousPeriod(period);

        BigDecimal currentValue =
                calculatePortfolioValue(
                        investments,
                        currentRange[1]
                );

        BigDecimal previousValue =
                calculatePortfolioValue(
                        investments,
                        previousRange[1]
                );

        BigDecimal currentInvestedCapital =
                calculateInvestedCapital(
                        investments,
                        currentRange[1]
                );

        BigDecimal previousInvestedCapital =
                calculateInvestedCapital(
                        investments,
                        previousRange[1]
                );

        BigDecimal currentReturn =
                currentValue.subtract(currentInvestedCapital);

        BigDecimal previousReturn =
                previousValue.subtract(previousInvestedCapital);

        return new InvestmentMetricsResponseDTO(
                currentValue,
                currentReturn,
                BigDecimal.ZERO,
                calculateTrend(
                        currentValue,
                        previousValue
                ),
                calculateTrend(
                        currentReturn,
                        previousReturn
                ),
                0.0
        );
    }

    @Override
    public InvestmentEvolutionResponseDTO getInvestmentEvolution(
            UUID userId,
            String period
    ) {
        List<InvestmentQueryPort.InvestmentData> investments =
                investmentQueryPort.findAllByUserId(userId);

        return switch (period.toLowerCase()) {
            case "week" ->
                    getWeeklyInvestmentEvolution(investments);
            case "month" ->
                    getMonthlyInvestmentEvolution(investments);
            case "year" ->
                    getYearlyInvestmentEvolution(investments);
            default ->
                    throw new IllegalArgumentException(
                            "Invalid period: " + period
                    );
        };
    }

    @Override
    public InvestmentDistributionResponseDTO getInvestmentDistribution(
            UUID userId,
            String period
    ) {
        List<InvestmentQueryPort.InvestmentData> investments =
                investmentQueryPort.findAllByUserId(userId)
                        .stream()
                        .filter(InvestmentQueryPort.InvestmentData::active)
                        .toList();

        BigDecimal totalValue = BigDecimal.ZERO;

        for (InvestmentQueryPort.InvestmentData inv : investments) {
            totalValue =
                    totalValue.add(
                            inv.getCurrentValue()
                    );
        }

        Map<String, List<InvestmentQueryPort.InvestmentData>> byType =
                investments.stream()
                        .collect(
                                Collectors.groupingBy(
                                        InvestmentQueryPort.InvestmentData::type
                                )
                        );

        List<InvestmentDistributionItemDTO> distribution =
                new ArrayList<>();

        int colorIndex = 0;

        for (Map.Entry<String, List<InvestmentQueryPort.InvestmentData>> entry :
                byType.entrySet()) {

            BigDecimal typeValue = BigDecimal.ZERO;

            for (InvestmentQueryPort.InvestmentData inv : entry.getValue()) {
                typeValue =
                        typeValue.add(
                                inv.getCurrentValue()
                        );
            }

            distribution.add(
                    new InvestmentDistributionItemDTO(
                            entry.getKey(),
                            typeValue,
                            calculatePercentage(
                                    typeValue,
                                    totalValue
                            ),
                            CHART_COLORS[
                                    colorIndex
                                            % CHART_COLORS.length
                                    ]
                    )
            );

            colorIndex++;
        }

        return new InvestmentDistributionResponseDTO(
                totalValue,
                distribution
        );
    }

    private InvestmentEvolutionResponseDTO getWeeklyInvestmentEvolution(
            List<InvestmentQueryPort.InvestmentData> investments
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        String[] dayNames = {
                "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
        };

        LocalDate today = timeProvider.localDateNow();
        LocalDate weekStart =
                today.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            LocalDate day =
                    weekStart.plusDays(i);

            if (day.isAfter(today)) {
                break;
            }

            labels.add(dayNames[i]);

            data.add(
                    getPortfolioValueAtDate(
                            investments,
                            day
                    )
            );
        }

        return new InvestmentEvolutionResponseDTO(
                "week",
                labels,
                data
        );
    }

    private InvestmentEvolutionResponseDTO getMonthlyInvestmentEvolution(
            List<InvestmentQueryPort.InvestmentData> investments
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        LocalDate today = timeProvider.localDateNow();

        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern(
                        "MMM",
                        Locale.of("es")
                );

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart =
                    today.minusMonths(i)
                            .withDayOfMonth(1);

            labels.add(
                    monthStart.format(fmt)
            );

            data.add(
                    getPortfolioValueAtDate(
                            investments,
                            monthStart
                                    .with(
                                            TemporalAdjusters
                                                    .lastDayOfMonth()
                                    )
                    )
            );
        }

        return new InvestmentEvolutionResponseDTO(
                "month",
                labels,
                data
        );
    }

    private InvestmentEvolutionResponseDTO getYearlyInvestmentEvolution(
            List<InvestmentQueryPort.InvestmentData> investments
    ) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        String[] monthNames = {
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
        };

        LocalDate today = timeProvider.localDateNow();

        for (int m = 1; m <= 12; m++) {
            LocalDate monthStart =
                    LocalDate.of(
                            today.getYear(),
                            m,
                            1
                    );

            if (monthStart.isAfter(today)) {
                break;
            }

            labels.add(
                    monthNames[m - 1]
            );

            data.add(
                    getPortfolioValueAtDate(
                            investments,
                            monthStart
                                    .with(
                                            TemporalAdjusters
                                                    .lastDayOfMonth()
                                    )
                    )
            );
        }

        return new InvestmentEvolutionResponseDTO(
                "year",
                labels,
                data
        );
    }

    private BigDecimal getPortfolioValueAtDate(
            List<InvestmentQueryPort.InvestmentData> investments,
            LocalDate date
    ) {
        BigDecimal total = BigDecimal.ZERO;

        for (InvestmentQueryPort.InvestmentData inv : investments) {
            if (inv.purchaseDate() != null
                    && !inv.purchaseDate().isAfter(date)
                    && inv.active()) {

                total = total.add(
                        inv.getCurrentValue()
                );
            }
        }

        return total;
    }

    private LocalDate[] getDateRange(
            String period
    ) {
        LocalDate today = timeProvider.localDateNow();

        return switch (period.toLowerCase()) {
            case "week" -> {
                LocalDate start =
                        today.with(DayOfWeek.MONDAY);

                LocalDate end =
                        start.plusDays(6);

                yield new LocalDate[]{start, end};
            }

            case "month" -> {
                LocalDate start =
                        today.withDayOfMonth(1);

                LocalDate end =
                        start.with(
                                TemporalAdjusters
                                        .lastDayOfMonth()
                        );

                yield new LocalDate[]{start, end};
            }

            case "year" -> {
                LocalDate start =
                        today.withDayOfYear(1);

                LocalDate end =
                        start.with(
                                TemporalAdjusters
                                        .lastDayOfYear()
                        );

                yield new LocalDate[]{start, end};
            }

            default ->
                    throw new IllegalArgumentException(
                            "Invalid period: " + period
                    );
        };
    }

    private LocalDate[] getPreviousPeriod(
            String period
    ) {
        LocalDate[] currentRange =
                getDateRange(period);

        LocalDate previousStart =
                switch (period.toLowerCase()) {
                    case "week" ->
                            currentRange[0].minusWeeks(1);

                    case "month" ->
                            currentRange[0].minusMonths(1);

                    case "year" ->
                            currentRange[0].minusYears(1);

                    default ->
                            throw new IllegalArgumentException(
                                    "Invalid period: " + period
                            );
                };

        LocalDate previousEnd =
                currentRange[0].minusDays(1);

        return new LocalDate[]{
                previousStart,
                previousEnd
        };
    }

    private double calculateTrend(
            BigDecimal current,
            BigDecimal previous
    ) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0
                    ? 100.0
                    : 0.0;
        }

        return current
                .subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(
                        previous.abs(),
                        1,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private double calculatePercentage(
            BigDecimal part,
            BigDecimal total
    ) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return part
                .multiply(new BigDecimal("100"))
                .divide(
                        total,
                        1,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private BigDecimal calculatePortfolioValue(
            List<InvestmentQueryPort.InvestmentData> investments,
            LocalDate date
    ) {
        BigDecimal total = BigDecimal.ZERO;

        for (InvestmentQueryPort.InvestmentData inv : investments) {
            if (inv.purchaseDate() != null
                    && !inv.purchaseDate().isAfter(date)
                    && inv.active()) {

                total = total.add(
                        inv.getCurrentValue()
                );
            }
        }

        return total;
    }

    private BigDecimal calculateInvestedCapital(
            List<InvestmentQueryPort.InvestmentData> investments,
            LocalDate date
    ) {
        BigDecimal total = BigDecimal.ZERO;

        for (InvestmentQueryPort.InvestmentData inv : investments) {
            if (inv.purchaseDate() != null
                    && !inv.purchaseDate().isAfter(date)
                    && inv.active()) {

                total = total.add(
                        inv.getInvestedCapital()
                );
            }
        }

        return total;
    }
}