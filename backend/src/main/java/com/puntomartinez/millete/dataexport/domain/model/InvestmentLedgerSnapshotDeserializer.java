package com.puntomartinez.millete.dataexport.domain.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public final class InvestmentLedgerSnapshotDeserializer
        extends JsonDeserializer<InvestmentLedgerSnapshot> {

    @Override
    public InvestmentLedgerSnapshot deserialize(
            JsonParser parser,
            DeserializationContext context
    ) throws IOException {

        JsonNode node =
                parser.getCodec().readTree(parser);

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isArray()) {
            return InvestmentLedgerSnapshot.emptyLegacy(
                    node.size()
            );
        }

        if (!node.isObject()) {
            return (InvestmentLedgerSnapshot)
                    context.handleUnexpectedToken(
                            InvestmentLedgerSnapshot.class,
                            parser
                    );
        }

        ObjectMapper mapper =
                (ObjectMapper) parser.getCodec();

        /*
         * Nuevo formato de Investments.
         */
        if (node.has("version")
                && node.has("trackingStartAt")) {

            int version =
                    node.get("version").asInt();

            Instant trackingStartAt =
                    mapper.treeToValue(
                            node.get("trackingStartAt"),
                            Instant.class
                    );

            return new InvestmentLedgerSnapshot(
                    version,
                    trackingStartAt,
                    list(
                            mapper,
                            node,
                            "userAssets",
                            InvestmentLedgerSnapshot
                                    .UserAssetSnapshot.class
                    ),
                    list(
                            mapper,
                            node,
                            "userAssetPrices",
                            InvestmentLedgerSnapshot
                                    .UserAssetPriceSnapshot.class
                    ),
                    list(
                            mapper,
                            node,
                            "holdings",
                            InvestmentLedgerSnapshot
                                    .HoldingSnapshot.class
                    ),
                    list(
                            mapper,
                            node,
                            "activities",
                            InvestmentLedgerSnapshot
                                    .ActivitySnapshot.class
                    ),
                    list(
                            mapper,
                            node,
                            "audits",
                            InvestmentLedgerSnapshot
                                    .ActivityAuditSnapshot.class
                    )
            );
        }

        /*
         * Formato legacy de Investments.
         *
         * No intentamos reconstruirlo porque el modelo antiguo
         * ya no existe. Se contabiliza y DataImportService
         * informará de que esos registros fueron omitidos.
         */
        return InvestmentLedgerSnapshot.emptyLegacy(
                countLegacyRecords(node)
        );
    }

    private int countLegacyRecords(
            JsonNode node
    ) {
        return arraySize(node, "assets")
                + arraySize(node, "holdings")
                + arraySize(node, "activities")
                + arraySize(node, "audit")
                + arraySize(node, "currencyHistory");
    }

    private int arraySize(
            JsonNode node,
            String field
    ) {
        JsonNode value = node.get(field);

        if (value == null
                || !value.isArray()) {
            return 0;
        }

        return value.size();
    }

    private <T> List<T> list(
            ObjectMapper mapper,
            JsonNode node,
            String key,
            Class<T> type
    ) throws IOException {

        JsonNode value =
                node.get(key);

        if (value == null
                || value.isNull()) {
            return List.of();
        }

        return mapper
                .readerForListOf(type)
                .readValue(value);
    }
}