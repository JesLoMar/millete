package com.puntomartinez.millete.dataexport.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDataSnapshot - DTO de exportación")
class UserDataSnapshotTest {

    @Test
    @DisplayName("Debe crear snapshot con todos los campos")
    void constructor_shouldCreateSnapshotWithAllFields() {
        UserDataSnapshot.SnapshotMetadata metadata = new UserDataSnapshot.SnapshotMetadata(
                "0.1.0", LocalDateTime.now(), "0.1.0"
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                metadata, List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null, null
        );

        assertNotNull(snapshot);
        assertEquals("0.1.0", snapshot.metadata().version());
        assertNotNull(snapshot.metadata().exportDate());
        assertEquals("0.1.0", snapshot.metadata().appVersion());
    }

    @Test
    @DisplayName("Debe lanzar error cuando metadata es null")
    void constructor_shouldThrow_whenMetadataNull() {
        assertThrows(IllegalArgumentException.class, () ->
            new UserDataSnapshot(null, List.of(), List.of(), List.of(), List.of(), List.of(),
                    null, null, null, null)
        );
    }

    @Test
    @DisplayName("SnapshotMetadata debe ser inmutable")
    void snapshotMetadata_shouldBeImmutableRecord() {
        LocalDateTime now = LocalDateTime.now();
        UserDataSnapshot.SnapshotMetadata metadata = new UserDataSnapshot.SnapshotMetadata(
                "0.1.0", now, "0.1.0"
        );

        assertEquals("0.1.0", metadata.version());
        assertEquals(now, metadata.exportDate());
        assertEquals("0.1.0", metadata.appVersion());
    }

    @Test
    @DisplayName("Debe permitir campos nulos para entidades opcionales")
    void constructor_shouldAllowNullOptionalFields() {
        UserDataSnapshot.SnapshotMetadata metadata = new UserDataSnapshot.SnapshotMetadata(
                "0.1.0", LocalDateTime.now(), "0.1.0"
        );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                metadata, null, null, null, null, null,
                null, null, null, null
        );

        assertNull(snapshot.categories());
        assertNull(snapshot.transactions());
        assertNull(snapshot.userPreferences());
    }
}
