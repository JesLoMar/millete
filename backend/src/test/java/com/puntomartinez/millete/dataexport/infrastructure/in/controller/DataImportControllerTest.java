package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.dataexport.application.services.DataImportService;
import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    void importData_shouldReturnSuccess_whenValidFile() {
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", "{}".getBytes());
        when(dataImportService.importUserData(any(), eq(userId))).thenReturn("Importación exitosa. 5 registros importados. v0.1.0");

        ResponseEntity<Map<String, Object>> response = controller.importData(file, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("Importación exitosa"));
    }

    @Test
    void importData_shouldReturnBadRequest_whenEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", new byte[0]);

        ResponseEntity<Map<String, Object>> response = controller.importData(file, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("ARCHIVO_VACIO", response.getBody().get("error"));
    }

    @Test
    void importData_shouldReturnBadRequest_whenInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "export.txt", "text/plain", "{}".getBytes());

        ResponseEntity<Map<String, Object>> response = controller.importData(file, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("FORMATO_NO_SOPORTADO", response.getBody().get("error"));
    }

    @Test
    void importData_shouldReturnBadRequest_whenServiceThrows() {
        MockMultipartFile file = new MockMultipartFile("file", "export.json", "application/json", "{}".getBytes());
        when(dataImportService.importUserData(any(), eq(userId))).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Map<String, Object>> response = controller.importData(file, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("ERROR_IMPORTACION", response.getBody().get("error"));
    }
}
