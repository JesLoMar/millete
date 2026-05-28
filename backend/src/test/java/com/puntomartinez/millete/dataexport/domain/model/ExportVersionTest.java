package com.puntomartinez.millete.dataexport.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ExportVersion - Versionado semántico")
class ExportVersionTest {

    @Test
    @DisplayName("Debe parsear versión válida")
    void shouldParseValidVersion() {
        ExportVersion v = ExportVersion.fromString("1.2.3");
        assertThat(v.major()).isEqualTo(1);
        assertThat(v.minor()).isEqualTo(2);
        assertThat(v.patch()).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe lanzar error con formato inválido")
    void shouldThrowWithInvalidFormat() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ExportVersion.fromString("1.2"));
    }

    @Test
    @DisplayName("Mismo MAJOR deben ser compatibles")
    void shouldBeCompatibleWithSameMajor() {
        ExportVersion v1 = new ExportVersion(1, 0, 0);
        ExportVersion v2 = new ExportVersion(1, 9, 9);
        assertThat(v1.isCompatibleWith(v2)).isTrue();
    }

    @Test
    @DisplayName("Distinto MAJOR deben ser incompatibles")
    void shouldNotBeCompatibleWithDifferentMajor() {
        ExportVersion v1 = new ExportVersion(1, 0, 0);
        ExportVersion v2 = new ExportVersion(2, 0, 0);
        assertThat(v1.isCompatibleWith(v2)).isFalse();
    }

    @Test
    @DisplayName("Versión menor necesita migración")
    void shouldNeedMigration() {
        ExportVersion v1 = new ExportVersion(1, 0, 0);
        ExportVersion v2 = new ExportVersion(1, 1, 0);
        assertThat(v1.needsMigration(v2)).isTrue();
    }

    @Test
    @DisplayName("Versión mayor no necesita migración")
    void shouldNotNeedMigration() {
        ExportVersion v1 = new ExportVersion(1, 1, 0);
        ExportVersion v2 = new ExportVersion(1, 0, 0);
        assertThat(v1.needsMigration(v2)).isFalse();
    }

    @Test
    @DisplayName("toString debe devolver formato correcto")
    void shouldFormatToString() {
        ExportVersion v = new ExportVersion(0, 0, 1);
        assertThat(v.toString()).isEqualTo("0.0.1");
    }
}