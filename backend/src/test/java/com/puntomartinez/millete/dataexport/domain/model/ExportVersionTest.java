package com.puntomartinez.millete.dataexport.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExportVersion")
class ExportVersionTest {

    @Test
    @DisplayName("Should parse valid version string")
    void shouldParseValidVersion() {
        ExportVersion v = ExportVersion.fromString("1.2.3");

        assertThat(v.major()).isEqualTo(1);
        assertThat(v.minor()).isEqualTo(2);
        assertThat(v.patch()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should throw on invalid format")
    void shouldThrowOnInvalidFormat() {
        assertThatThrownBy(() -> ExportVersion.fromString("1.2"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw on null version")
    void shouldThrowOnNullVersion() {
        assertThatThrownBy(() -> ExportVersion.fromString(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw on blank version")
    void shouldThrowOnBlankVersion() {
        assertThatThrownBy(() -> ExportVersion.fromString("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Same major versions should be compatible")
    void shouldBeCompatibleWithSameMajor() {
        ExportVersion v1 = new ExportVersion(1, 0, 0);
        ExportVersion v2 = new ExportVersion(1, 9, 9);

        assertThat(v1.isCompatibleWith(v2)).isTrue();
    }

    @Test
    @DisplayName("Different major versions should not be compatible")
    void shouldNotBeCompatibleWithDifferentMajor() {
        ExportVersion v1 = new ExportVersion(1, 0, 0);
        ExportVersion v2 = new ExportVersion(2, 0, 0);

        assertThat(v1.isCompatibleWith(v2)).isFalse();
    }

    @Test
    @DisplayName("Lower version should need migration")
    void shouldNeedMigration() {
        ExportVersion v1 = new ExportVersion(1, 0, 0);
        ExportVersion v2 = new ExportVersion(1, 1, 0);

        assertThat(v1.needsMigration(v2)).isTrue();
    }

    @Test
    @DisplayName("Higher version should not need migration")
    void shouldNotNeedMigration() {
        ExportVersion v1 = new ExportVersion(1, 1, 0);
        ExportVersion v2 = new ExportVersion(1, 0, 0);

        assertThat(v1.needsMigration(v2)).isFalse();
    }

    @Test
    @DisplayName("toString should return correct format")
    void shouldFormatToString() {
        ExportVersion v = new ExportVersion(0, 2, 0);

        assertThat(v.toString()).isEqualTo("0.2.0");
    }
}