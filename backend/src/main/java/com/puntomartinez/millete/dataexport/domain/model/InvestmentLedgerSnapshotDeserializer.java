package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/** Reads the v0.3 ledger object and counts (but never converts) pre-ledger flat rows. */
public final class InvestmentLedgerSnapshotDeserializer extends JsonDeserializer<InvestmentLedgerSnapshot> {
    @Override
    public InvestmentLedgerSnapshot deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) return null;
        if (node.isArray()) return InvestmentLedgerSnapshot.emptyLegacy(node.size());
        if (!node.isObject()) return (InvestmentLedgerSnapshot) context.handleUnexpectedToken(InvestmentLedgerSnapshot.class, parser);
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return new InvestmentLedgerSnapshot(
                list(mapper, node, "assets", InvestmentLedgerSnapshot.AssetSnapshot.class),
                list(mapper, node, "holdings", InvestmentLedgerSnapshot.HoldingSnapshot.class),
                list(mapper, node, "activities", InvestmentLedgerSnapshot.ActivitySnapshot.class),
                list(mapper, node, "audit", InvestmentLedgerSnapshot.ActivityAuditSnapshot.class),
                list(mapper, node, "currencyHistory", InvestmentLedgerSnapshot.CurrencyPeriodSnapshot.class));
    }

    private <T> List<T> list(ObjectMapper mapper, JsonNode node, String key, Class<T> type) throws IOException {
        JsonNode value = node.get(key);
        return value == null || value.isNull() ? List.of() : mapper.readerForListOf(type).readValue(value);
    }
}
