package com.puntomartinez.millete.dataexport.domain.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;

/**
 * Utilidad de normalización temporal aplicada a los snapshots de exportación.
 *
 * <p>La normalización temporal de Millete (Instant para marcas de tiempo,
 * LocalDate para fechas de calendario) es un cambio de formato que NO
 * incrementa la versión del snapshot: sigue siendo 0.2.0. Los archivos
 * antiguos contienen cadenas ISO sin zona ({@code "2024-05-01T14:30:00"});
 * esta utilidad las reinterpreta como UTC añadiendo el sufijo {@code Z}, y
 * trunca la hora de {@code transactions.date} a día (decisión acordada:
 * se preserva únicamente la parte de fecha).</p>
 *
 * <p>Se opera a nivel de JSON para poder transformar los timestamps antes de
 * que Jackson intente deserializarlos como {@code Instant}/{@code LocalDate}.</p>
 */
public final class TemporalNormalizer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TemporalNormalizer() {
    }

    /**
     * Reescribe el JSON del snapshot con timestamps en UTC explícito ({@code Z})
     * y la fecha de transacción truncada a día. No altera la marca de versión.
     */
    public static UserDataSnapshot normalize(UserDataSnapshot snapshot) {
        ObjectNode root = MAPPER.valueToTree(snapshot);

        if (root.get("metadata") != null && root.get("metadata").isObject()) {
            ObjectNode metadata = (ObjectNode) root.get("metadata");
            normalizeTimestamp(metadata, "exportDate");
        }

        normalizeCollection(root, "transactions", true);
        normalizeCollection(root, "categories", false);
        normalizeCollection(root, "plannedTransactions", false);
        normalizeCollection(root, "investments", false);
        normalizeCollection(root, "savingsGoals", false);
        if (root.get("userPreferences") != null && root.get("userPreferences").isObject()) {
            ObjectNode prefs = (ObjectNode) root.get("userPreferences");
            normalizeTimestamp(prefs, "createdAt");
            normalizeTimestamp(prefs, "modifiedAt");
        }

        return MAPPER.convertValue(root, UserDataSnapshot.class);
    }

    private static void normalizeCollection(
            ObjectNode root, String field, boolean truncateDate) {
        JsonNode node = root.get(field);
        if (node == null || !node.isArray()) {
            return;
        }
        ArrayNode array = (ArrayNode) node;
        for (int i = 0; i < array.size(); i++) {
            JsonNode element = array.get(i);
            if (!element.isObject()) {
                continue;
            }
            ObjectNode object = (ObjectNode) element;
            normalizeTimestamp(object, "createdAt");
            normalizeTimestamp(object, "modifiedAt");
            if (truncateDate) {
                truncateToDay(object, "date");
            }
        }
    }

    /**
     * Convierte un timestamp ISO local (sin zona) en un instante UTC explícito.
     */
    private static void normalizeTimestamp(ObjectNode object, String field) {
        if (object == null) {
            return;
        }
        JsonNode value = object.get(field);
        if (value == null || !value.isTextual()) {
            return;
        }
        object.put(field, asUtc(value.asText()));
    }

    /**
     * Trunca un timestamp a su parte de fecha (yyyy-MM-dd), descartando la hora.
     */
    private static void truncateToDay(ObjectNode object, String field) {
        JsonNode value = object.get(field);
        if (value == null || !value.isTextual()) {
            return;
        }
        String text = value.asText();
        int tIndex = text.indexOf('T');
        object.put(field, tIndex > 0 ? text.substring(0, tIndex) : text);
    }

    private static String asUtc(String raw) {
        String trimmed = raw.trim();
        if (trimmed.endsWith("Z") || trimmed.contains("+") || hasNegativeOffset(trimmed)) {
            return trimmed;
        }
        // "2024-05-01T14:30" o "2024-05-01T14:30:00[.SSS]" -> añadir Z
        String body = trimmed.replace(' ', 'T');
        if (!body.contains(":")) {
            // ya era una fecha suelta: se mantiene como fecha UTC medianoche
            return body + "T00:00:00Z";
        }
        if (body.length() == 16) {
            // "2024-05-01T14:30" -> completar segundos
            body = body + ":00";
        }
        return body + "Z";
    }

    private static boolean hasNegativeOffset(String value) {
        int dash = value.lastIndexOf('-');
        return dash > 7; // un '-' tras la posición de la fecha indica offset negativo
    }
}
