package com.puntomartinez.millete.users.domain.validation;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

/**
 * Validador de dominios para identificadores de zona horaria.
 *
 * <p>Millete solo acepta identificadores de zona IANA válidos
 * (por ejemplo, {@code Europe/Madrid}, {@code America/Bogota}). Se rechazan
 * explícitamente los formatos de offset fijo como {@code UTC}, {@code GMT}
 * o {@code +02:00}, ya que no representan una zona geográfica real y no
 * gestionan cambios de horario de verano.</p>
 */
public final class ZoneIdValidator {

    /**
     * Zona horaria por defecto para usuarios nuevos y existentes que aún
     * no han configurado la suya. Es un valor técnico neutral: el frontend
     * debe invitar al usuario a configurarla en el primer inicio de sesión.
     */
    public static final String DEFAULT_TIMEZONE = "UTC";

    private static final int MAX_LENGTH = 64;

    private ZoneIdValidator() {
    }

    /**
     * Devuelve la zona horaria normalizada (trim). Si el valor es nulo o
     * está en blanco, devuelve la zona por defecto ({@value #DEFAULT_TIMEZONE}).
     *
     * @throws InvalidInputException si el valor no es un identificador IANA válido
     */
    public static String normalizeOrDefault(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_TIMEZONE;
        }
        requireValid(timezone);
        return timezone.trim();
    }

    /**
     * Valida que el valor sea un identificador de zona IANA aceptable.
     *
     * @throws InvalidInputException si el valor es inválido
     */
    public static void requireValid(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            throw new InvalidInputException(
                    "La zona horaria no puede estar vacía"
            );
        }

        String trimmed = timezone.trim();

        if (trimmed.length() > MAX_LENGTH) {
            throw new InvalidInputException(
                    "La zona horaria no puede exceder " + MAX_LENGTH + " caracteres"
            );
        }

        // Rechazo explícito de offsets/fijos no-IANA: ZoneId.of("UTC") sería
        // válido para Java pero no queremos permitirlos como valor de usuario.
        if (isFixedOffsetStyle(trimmed)) {
            throw new InvalidInputException(
                    "La zona horaria debe ser un identificador IANA válido "
                            + "(por ejemplo, Europe/Madrid)"
            );
        }

        try {
            ZoneId.of(trimmed);
        } catch (java.time.DateTimeException ex) {
            // ZoneRulesException es subclase de DateTimeException, basta capturarlo.
            throw new InvalidInputException(
                    "Zona horaria no reconocida: " + trimmed
                            + ". Usa un identificador IANA válido (por ejemplo, Europe/Madrid)"
            );
        }
    }

    private static boolean isFixedOffsetStyle(String value) {
        String upper = value.toUpperCase();
        return upper.startsWith("UTC")
                || upper.startsWith("GMT")
                || upper.startsWith("Z")
                || value.startsWith("+")
                || value.startsWith("-");
    }
}
