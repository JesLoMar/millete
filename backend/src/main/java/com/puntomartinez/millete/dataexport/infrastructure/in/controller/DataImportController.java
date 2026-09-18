package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.dataexport.application.services.DataImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Controller de importación de datos.
 *
 * <p>LÍMITE DE TAMAÑO: El tamaño máximo se controla en dos niveles:
 * <ul>
 *   <li>Spring: {@code spring.servlet.multipart.max-file-size=50MB}
 *       y {@code spring.servlet.multipart.max-request-size=50MB}
 *       en application.properties.</li>
 *   <li>Service: validación adicional en DataImportService.</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/data")
public class DataImportController {

    private final DataImportService dataImportService;

    public DataImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> importData(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        log.info("Solicitud de importación para usuario: {}", userId);

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "ARCHIVO_VACIO",
                            "message", "El archivo no puede estar vacío"
                    ));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null
                && !originalFilename.toLowerCase().endsWith(".json")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "FORMATO_NO_SOPORTADO",
                            "message", "Solo se aceptan archivos JSON"
                    ));
        }

        try {
            String summary = dataImportService.importUserData(file, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", summary
            ));
        } catch (RuntimeException e) {
            log.error("Error en importación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "error", "ERROR_IMPORTACION",
                            "message", e.getMessage() != null
                                    ? e.getMessage()
                                    : "Error al importar el archivo."
                    ));
        }
    }
}