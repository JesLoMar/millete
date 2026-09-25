package com.puntomartinez.millete.shared.domain.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Puerto de salida (DDD) que abstrae el reloj del sistema.
 *
 * <p>Toda la lógica de negocio y de dominio debe obtener el tiempo actual a
 * través de este puerto en lugar de invocar directamente
 * {@code LocalDateTime.now()}, {@code LocalDate.now()} o {@code Instant.now()}.
 * Esto garantiza:</p>
 *
 * <ul>
 *   <li><b>Determinismo en tests</b>: permite inyectar relojes fijos o
 *       desplazados (por ejemplo, para simular usuarios en distintas zonas
 *       horarias).</li>
 *   <li><b>Consistencia</b>: una misma operación ve siempre el mismo instante,
 *       aunque su ejecución se prolongue.</li>
 *   <li><b>Semántica temporal explícita</b>: cada método expresa si lo que se
 *       necesita es un instante ({@link #instantNow()}), una fecha de calendario
 *       ({@link #localDateNow()}) o un date-time local del servidor
 *       ({@link #localDateTimeNow()}).</li>
 * </ul>
 */
public interface TimeProvider {

    /**
     * Devuelve el instante actual (UTC). Es el tipo canónico para marcas de
     * tiempo de auditoría, expiraciones y cualquier "momento" real.
     */
    Instant instantNow();

    /**
     * Devuelve la fecha de calendario actual según la zona horaria de referencia
     * del proveedor (ver {@link #zone()}).
     */
    LocalDate localDateNow();

    /**
     * Devuelve el date-time local actual según la zona horaria de referencia
     * del proveedor (ver {@link #zone()}).
     *
     * @deprecated Los modelos de dominio deben migrar progresivamente a
     *             {@link #instantNow()} (para momentos) y
     *             {@link #localDateNow()} (para fechas de calendario). Este
     *             método existe únicamente para mantener compatibilidad durante
     *             la normalización temporal.
     */
    @Deprecated(forRemoval = true)
    default LocalDateTime localDateTimeNow() {
        return LocalDateTime.ofInstant(instantNow(), zone());
    }

    /**
     * Zona horaria de referencia utilizada para derivar fechas locales.
     */
    ZoneId zone();
}
