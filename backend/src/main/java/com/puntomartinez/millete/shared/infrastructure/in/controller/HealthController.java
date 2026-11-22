package com.puntomartinez.millete.shared.infrastructure.in.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    public static final String PATH = "/api/v1/health";

    @GetMapping(PATH)
    public ResponseEntity<Void> health() {
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore())
                .build();
    }
}
