package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.dataexport.application.services.DataExportService;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/data")
public class DataExportController {

    private final DataExportService dataExportService;

    public DataExportController(DataExportService dataExportService) {
        this.dataExportService = dataExportService;
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDataSnapshot> exportData(Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        log.info("Solicitud de exportación para usuario: {}", userId);

        UserDataSnapshot snapshot = dataExportService.exportAllUserData(userId);

        // Headers para forzar la descarga como archivo físico
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=familybudget_export.json");
        headers.add("X-Export-Version", snapshot.metadata().version());
        headers.add("X-Export-Date", snapshot.metadata().exportDate().toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(snapshot);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportDataAsCsv(Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        log.info("Solicitud de exportación CSV para usuario: {}", userId);

        byte[] csv = dataExportService.exportUserDataAsCsv(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=familybudget_export.csv");

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportDataAsPdf(Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        log.info("Solicitud de exportación PDF para usuario: {}", userId);

        byte[] pdf = dataExportService.exportUserDataAsPdf(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=familybudget_export.pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}