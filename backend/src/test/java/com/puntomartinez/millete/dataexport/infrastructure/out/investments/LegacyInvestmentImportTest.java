package com.puntomartinez.millete.dataexport.infrastructure.out.investments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntomartinez.millete.dataexport.domain.model.InvestmentLedgerSnapshot;
import com.puntomartinez.millete.shared.infrastructure.config.JacksonConfig;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacyInvestmentImportTest {

    private final UUID userId = UUID.randomUUID();
    private final AtomicInteger restoreCalls = new AtomicInteger();
    private final ObjectMapper mapper = new JacksonConfig().objectMapper();
    private final InvestmentImportAdapter adapter = new InvestmentImportAdapter(
            (id, snapshot) -> restoreCalls.incrementAndGet(), mapper);

    @Test
    void skipsEmptyLegacySections() throws Exception {
        for (String json : new String[]{"[]", "{\"assets\":[]}"}) {
            var snapshot = mapper.readValue(json, InvestmentLedgerSnapshot.class);
            assertThat(adapter.importInvestments(snapshot, userId)).isZero();
        }
        assertThat(restoreCalls.get()).isZero();
    }

    @Test
    void countsButDoesNotRestoreLegacyRecords() throws Exception {
        for (String json : new String[]{"[{}]", "{\"assets\":[{}]}"}) {
            var snapshot = mapper.readValue(json, InvestmentLedgerSnapshot.class);
            assertThat(snapshot.legacyRecordsSkipped()).isEqualTo(1);
            assertThat(adapter.importInvestments(snapshot, userId)).isZero();
        }
        assertThat(restoreCalls.get()).isZero();
    }

    @Test
    void stillRestoresValidCurrentLedgerAndRejectsInvalidVersion() throws Exception {
        String current = """
                {"version":%d,"trackingStartAt":"2025-01-01T12:00:00Z"}
                """;
        var valid = mapper.readValue(current.formatted(1), InvestmentLedgerSnapshot.class);
        var invalid = mapper.readValue(current.formatted(0), InvestmentLedgerSnapshot.class);

        assertThat(adapter.importInvestments(valid, userId)).isZero();
        assertThatThrownBy(() -> adapter.importInvestments(invalid, userId))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(restoreCalls.get()).isEqualTo(1);
    }
}
