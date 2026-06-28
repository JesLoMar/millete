package com.puntomartinez.millete.dataexport.infrastructure.in.controller;

import com.puntomartinez.millete.shared.infrastructure.in.controller.dto.JwtUser;
import com.puntomartinez.millete.dataexport.application.services.DataExportService;
import com.puntomartinez.millete.dataexport.domain.model.PeriodType;
import com.puntomartinez.millete.dataexport.domain.model.UserDataSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();

        log.info("Solicitud de exportación para usuario: {}", userId);

        UserDataSnapshot snapshot = dataExportService.exportAllUserData(userId);

        // Headers para forzar la descarga como archivo físico
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=millete_export.json");
        headers.add("X-Export-Version", snapshot.metadata().version());
        headers.add("X-Export-Date", snapshot.metadata().exportDate().toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(snapshot);
    }

    @GetMapping("/export/zip")
    public ResponseEntity<byte[]> exportDataAsZip(Authentication authentication) {
        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        log.info("Solicitud de exportación ZIP para usuario: {}", userId);

        byte[] zip = dataExportService.exportUserDataAsZip(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=millete_export.zip");

        return new ResponseEntity<>(zip, headers, HttpStatus.OK);
    }

    @GetMapping("/export/csv/{entityType}")
    public ResponseEntity<byte[]> exportDataAsCsv(
            @PathVariable String entityType,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        log.info("Solicitud de exportación CSV ({}) para usuario: {}", entityType, userId);

        byte[] csv = dataExportService.exportUserDataAsCsv(userId, entityType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=millete_" + entityType + ".csv");

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportDataAsPdf(
            @RequestParam(defaultValue = "1m") String period,
            Authentication authentication) {

        UUID userId = ((JwtUser) authentication.getPrincipal()).getId();
        PeriodType periodType = PeriodType.fromCode(period);
        log.info("Solicitud de exportación PDF para usuario: {} (periodo: {})", userId, periodType.getCode());

        byte[] pdf = dataExportService.exportUserDataAsPdf(userId, periodType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=millete_financial_data_" + periodType.getCode() + ".pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}