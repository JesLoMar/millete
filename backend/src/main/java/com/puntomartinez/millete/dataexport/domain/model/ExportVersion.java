package com.puntomartinez.millete.dataexport.domain.model;

import java.util.regex.Pattern;

public record ExportVersion(int major, int minor, int patch) implements Comparable<ExportVersion> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

    // Incrementar cuando cambie el esquema de exportación
    public static final ExportVersion CURRENT = new ExportVersion(0, 0, 1);

    public static ExportVersion fromString(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("La versión no puede ser nula o vacía");
        }

        var matcher = VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Formato de versión inválido: '" + version + "'");
        }

        return new ExportVersion(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }

    // Mismo MAJOR = compatible (puede necesitar migración)
    public boolean isCompatibleWith(ExportVersion other) {
        return this.major == other.major;
    }

    // ¿Esta versión es más antigua que target?
    public boolean needsMigration(ExportVersion target) {
        return this.compareTo(target) < 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public int compareTo(ExportVersion other) {
        int majorCompare = Integer.compare(this.major, other.major);
        if (majorCompare != 0) return majorCompare;

        int minorCompare = Integer.compare(this.minor, other.minor);
        if (minorCompare != 0) return minorCompare;

        return Integer.compare(this.patch, other.patch);
    }
}