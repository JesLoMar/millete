package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.dataexport.application.services.DataExportService;
import com.puntomartinez.millete.dataexport.domain.model.PeriodType;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataExportController")
class DataExportControllerTest {

    @Mock
    private DataExportService dataExportService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DataExportController controller;

    private UUID userId;
    private JwtUser jwtUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        jwtUser = new JwtUser(userId, "test@example.com", "Test User");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    private UserDataSnapshot createSnapshot() {
        return new UserDataSnapshot(
                new UserDataSnapshot.SnapshotMetadata(
                        "0.2.0", LocalDateTime.now(), "0.2.0"
                ),
                List.of(), List.of(), List.of(), List.of(), List.of(), null
        );
    }

    @Test
    @DisplayName("Should return snapshot with headers")
    void exportDataShouldReturnSnapshotWithHeaders() {
        UserDataSnapshot snapshot = createSnapshot();
        when(dataExportService.exportAllUserData(userId)).thenReturn(snapshot);

        ResponseEntity<UserDataSnapshot> response = controller.exportData(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().get("Content-Disposition")).isNotNull();
        assertThat(response.getHeaders().get("Content-Disposition").getFirst())
                .contains("millete_export.json");
        assertThat(response.getHeaders().get("X-Export-Version")).isNotNull();
        assertThat(response.getHeaders().get("X-Export-Date")).isNotNull();
    }

    @Test
    @DisplayName("Should return zip bytes")
    void exportDataAsZipShouldReturnZipBytes() {
        when(dataExportService.exportUserDataAsZip(userId))
                .thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<byte[]> response = controller.exportDataAsZip(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getHeaders().get("Content-Disposition").getFirst())
                .contains("millete_export.zip");
    }

    @Test
    @DisplayName("Should return csv bytes")
    void exportDataAsCsvShouldReturnCsvBytes() {
        when(dataExportService.exportUserDataAsCsv(userId, "categories"))
                .thenReturn(new byte[]{4, 5, 6});

        ResponseEntity<byte[]> response =
                controller.exportDataAsCsv("categories", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().get("Content-Disposition").getFirst())
                .contains("millete_categories.csv");
    }

    @Test
    @DisplayName("Should return pdf bytes")
    void exportDataAsPdfShouldReturnPdfBytes() {
        when(dataExportService.exportUserDataAsPdf(eq(userId), any(PeriodType.class)))
                .thenReturn(new byte[]{7, 8, 9});

        ResponseEntity<byte[]> response =
                controller.exportDataAsPdf("1m", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().get("Content-Disposition").getFirst())
                .contains("millete_financial_data_1m.pdf");
    }
}