package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.dataexport.application.services.DataImportService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataImportController")
class DataImportControllerTest {

    @Mock
    private DataImportService dataImportService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DataImportController controller;

    private UUID userId;
    private JwtUser jwtUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        jwtUser = new JwtUser(userId, "test@example.com", "Test User");
        when(authentication.getPrincipal()).thenReturn(jwtUser);
    }

    @Test
    @DisplayName("importData should return success when valid file")
    void importDataShouldReturnSuccessWhenValidFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json", "{}".getBytes()
        );

        when(dataImportService.importUserData(any(), eq(userId)))
                .thenReturn("Importación exitosa. 5 registros importados. v0.2.0");

        ResponseEntity<Map<String, Object>> response =
                controller.importData(file, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message").toString())
                .contains("Importación exitosa");
    }

    @Test
    @DisplayName("importData should return bad request when empty file")
    void importDataShouldReturnBadRequestWhenEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json", new byte[0]
        );

        ResponseEntity<Map<String, Object>> response =
                controller.importData(file, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error")).isEqualTo("ARCHIVO_VACIO");
    }

    @Test
    @DisplayName("importData should return bad request when invalid extension")
    void importDataShouldReturnBadRequestWhenInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.txt", "text/plain", "{}".getBytes()
        );

        ResponseEntity<Map<String, Object>> response =
                controller.importData(file, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error")).isEqualTo("FORMATO_NO_SOPORTADO");
    }

    @Test
    @DisplayName("importData should return bad request when service throws")
    void importDataShouldReturnBadRequestWhenServiceThrows() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "export.json", "application/json", "{}".getBytes()
        );

        when(dataImportService.importUserData(any(), eq(userId)))
                .thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response =
                controller.importData(file, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("error")).isEqualTo("ERROR_IMPORTACION");
    }
}