package com.puntomartinez.millete.dataexport.domain.model;

import java.time.LocalDate;

public enum PeriodType {
    ONE_MONTH("1m", 1),
    THREE_MONTHS("3m", 3),
    SIX_MONTHS("6m", 6),
    ONE_YEAR("1y", 12);

    private final String code;
    private final int months;

    PeriodType(String code, int months) {
        this.code = code;
        this.months = months;
    }

    public String getCode() { return code; }
    public int getMonths() { return months; }

    public LocalDate getStartDate() {
        return LocalDate.now().minusMonths(months);
    }

    public LocalDate getEndDate() {
        return LocalDate.now();
    }

    public static PeriodType fromCode(String code) {
        for (PeriodType pt : values()) {
            if (pt.code.equalsIgnoreCase(code)) return pt;
        }
        throw new IllegalArgumentException("Periodo no válido: " + code + ". Usar: 1m, 3m, 6m, 1y");
    }

    public String getDisplayName() {
        return switch (this) {
            case ONE_MONTH -> "1 month";
            case THREE_MONTHS -> "3 months";
            case SIX_MONTHS -> "6 months";
            case ONE_YEAR -> "1 year";
        };
    }
}