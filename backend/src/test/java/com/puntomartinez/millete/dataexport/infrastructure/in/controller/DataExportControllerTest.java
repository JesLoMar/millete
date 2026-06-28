package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.dataexport.application.services.DataExportService;
import com.puntomartinez.millete.dataexport.domain.model.*;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
                new UserDataSnapshot.SnapshotMetadata("0.1.0", LocalDateTime.now(), "0.1.0"),
                List.of(), List.of(), List.of(), List.of(), List.of(),
                null, null, null, null
        );
    }

    @Test
    void exportData_shouldReturnSnapshotWithHeaders() {
        UserDataSnapshot snapshot = createSnapshot();
        when(dataExportService.exportAllUserData(userId)).thenReturn(snapshot);

        ResponseEntity<UserDataSnapshot> response = controller.exportData(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getHeaders().get("Content-Disposition"));
        assertTrue(response.getHeaders().get("Content-Disposition").get(0).contains("millete_export.json"));
        assertNotNull(response.getHeaders().get("X-Export-Version"));
        assertNotNull(response.getHeaders().get("X-Export-Date"));
    }

    @Test
    void exportDataAsZip_shouldReturnZipBytes() {
        when(dataExportService.exportUserDataAsZip(userId)).thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<byte[]> response = controller.exportDataAsZip(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().length);
        assertTrue(response.getHeaders().get("Content-Disposition").get(0).contains("millete_export.zip"));
    }

    @Test
    void exportDataAsCsv_shouldReturnCsvBytes() {
        when(dataExportService.exportUserDataAsCsv(userId, "categories")).thenReturn(new byte[]{4, 5, 6});

        ResponseEntity<byte[]> response = controller.exportDataAsCsv("categories", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().get("Content-Disposition").get(0).contains("millete_categories.csv"));
    }

    @Test
    void exportDataAsPdf_shouldReturnPdfBytes() {
        when(dataExportService.exportUserDataAsPdf(eq(userId), any(PeriodType.class))).thenReturn(new byte[]{7, 8, 9});

        ResponseEntity<byte[]> response = controller.exportDataAsPdf("1m", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().get("Content-Disposition").get(0).contains("millete_financial_data_1m.pdf"));
    }
}
