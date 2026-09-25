package com.puntomartinez.millete.dataexport.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserDataSnapshot")
class UserDataSnapshotTest {

    @Test
    @DisplayName("Should create snapshot with all fields")
    void shouldCreateSnapshotWithAllFields() {
        UserDataSnapshot.SnapshotMetadata metadata =
                new UserDataSnapshot.SnapshotMetadata(
                        "0.2.0", Instant.now(), "0.2.0"
                );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                metadata, List.of(), List.of(), List.of(), List.of(), List.of(), null
        );

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.metadata().version()).isEqualTo("0.2.0");
        assertThat(snapshot.metadata().exportDate()).isNotNull();
        assertThat(snapshot.metadata().appVersion()).isEqualTo("0.2.0");
    }

    @Test
    @DisplayName("Should throw when metadata is null")
    void shouldThrowWhenMetadataIsNull() {
        assertThatThrownBy(() ->
                new UserDataSnapshot(
                        null, List.of(), List.of(), List.of(), List.of(), List.of(), null
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SnapshotMetadata should be immutable record")
    void snapshotMetadataShouldBeImmutable() {
        LocalDateTime now = Instant.now();
        UserDataSnapshot.SnapshotMetadata metadata =
                new UserDataSnapshot.SnapshotMetadata("0.2.0", now, "0.2.0");

        assertThat(metadata.version()).isEqualTo("0.2.0");
        assertThat(metadata.exportDate()).isEqualTo(now);
        assertThat(metadata.appVersion()).isEqualTo("0.2.0");
    }

    @Test
    @DisplayName("Should allow null optional fields")
    void shouldAllowNullOptionalFields() {
        UserDataSnapshot.SnapshotMetadata metadata =
                new UserDataSnapshot.SnapshotMetadata(
                        "0.2.0", Instant.now(), "0.2.0"
                );

        UserDataSnapshot snapshot = new UserDataSnapshot(
                metadata, null, null, null, null, null, null
        );

        assertThat(snapshot.categories()).isNull();
        assertThat(snapshot.transactions()).isNull();
        assertThat(snapshot.userPreferences()).isNull();
    }
}