package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.dataexport.application.services.DataImportService;
import com.puntomartinez.millete.dataexport.domain.exception.OwnershipException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/data")
public class DataImportController {

    private final DataImportService dataImportService;

    public DataImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    /**
     * Importa datos desde un archivo JSON previamente exportado.
     * Validaciones:
     * - El archivo no puede estar vacío
     * - El archivo debe pertenecer al usuario autenticado
     * - La versión del archivo debe ser compatible
     *
     * @param file           Archivo JSON con los datos a importar
     * @param authentication Usuario autenticado
     * @return Mensaje de éxito con resumen de la importación
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importData(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        log.info("Solicitud de importación para usuario: {}", userId);

        // ─── Validación: archivo no vacío ──────────────────
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "ARCHIVO_VACIO",
                            "message", "El archivo no puede estar vacío"
                    ));
        }

        // ─── Validación: tipo de archivo ───────────────────
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".json")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "FORMATO_NO_SOPORTADO",
                            "message", "Solo se aceptan archivos JSON"
                    ));
        }

        try {
            // ─── Delegar al servicio ───────────────────────
            String summary = dataImportService.importUserData(file, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", summary
            ));

        } catch (OwnershipException e) {
            // ─── Error de propiedad ────────────────────────
            log.warn("Importación rechazada: {}", e.getErrorCode());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "success", false,
                            "error", e.getErrorCode(),
                            "message", e.getMessage()
                    ));

        } catch (RuntimeException e) {
            // ─── Error de formato/versión/BD ───────────────
            log.error("Error en importación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "ERROR_IMPORTACION",
                            "message", e.getMessage()
                    ));
        }
    }
}