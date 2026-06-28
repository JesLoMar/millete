package com.puntomartinez.millete.dataexport.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PeriodType - Periodos de exportación PDF")
class PeriodTypeTest {

    @Test
    @DisplayName("fromCode debe devolver el periodo correcto")
    void fromCode_shouldReturnCorrectPeriod() {
        assertEquals(PeriodType.ONE_MONTH, PeriodType.fromCode("1m"));
        assertEquals(PeriodType.THREE_MONTHS, PeriodType.fromCode("3m"));
        assertEquals(PeriodType.SIX_MONTHS, PeriodType.fromCode("6m"));
        assertEquals(PeriodType.ONE_YEAR, PeriodType.fromCode("1y"));
    }

    @Test
    @DisplayName("fromCode debe ser case-insensitive")
    void fromCode_shouldBeCaseInsensitive() {
        assertEquals(PeriodType.ONE_MONTH, PeriodType.fromCode("1M"));
        assertEquals(PeriodType.ONE_YEAR, PeriodType.fromCode("1Y"));
    }

    @Test
    @DisplayName("fromCode debe lanzar error con código inválido")
    void fromCode_shouldThrow_whenInvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> PeriodType.fromCode("invalid"));
    }

    @Test
    @DisplayName("getStartDate debe calcular fecha correcta")
    void getStartDate_shouldCalculateCorrectDate() {
        LocalDate today = LocalDate.now();
        
        assertEquals(today.minusMonths(1), PeriodType.ONE_MONTH.getStartDate());
        assertEquals(today.minusMonths(3), PeriodType.THREE_MONTHS.getStartDate());
        assertEquals(today.minusMonths(6), PeriodType.SIX_MONTHS.getStartDate());
        assertEquals(today.minusMonths(12), PeriodType.ONE_YEAR.getStartDate());
    }

    @Test
    @DisplayName("getEndDate debe devolver fecha actual")
    void getEndDate_shouldReturnToday() {
        assertEquals(LocalDate.now(), PeriodType.ONE_MONTH.getEndDate());
    }

    @Test
    @DisplayName("getDisplayName debe devolver nombre legible")
    void getDisplayName_shouldReturnReadableName() {
        assertEquals("1 month", PeriodType.ONE_MONTH.getDisplayName());
        assertEquals("3 months", PeriodType.THREE_MONTHS.getDisplayName());
        assertEquals("6 months", PeriodType.SIX_MONTHS.getDisplayName());
        assertEquals("1 year", PeriodType.ONE_YEAR.getDisplayName());
    }

    @Test
    @DisplayName("getCode debe devolver código correcto")
    void getCode_shouldReturnCorrectCode() {
        assertEquals("1m", PeriodType.ONE_MONTH.getCode());
        assertEquals("3m", PeriodType.THREE_MONTHS.getCode());
        assertEquals("6m", PeriodType.SIX_MONTHS.getCode());
        assertEquals("1y", PeriodType.ONE_YEAR.getCode());
    }

    @Test
    @DisplayName("getMonths debe devolver meses correctos")
    void getMonths_shouldReturnCorrectMonths() {
        assertEquals(1, PeriodType.ONE_MONTH.getMonths());
        assertEquals(3, PeriodType.THREE_MONTHS.getMonths());
        assertEquals(6, PeriodType.SIX_MONTHS.getMonths());
        assertEquals(12, PeriodType.ONE_YEAR.getMonths());
    }
}
