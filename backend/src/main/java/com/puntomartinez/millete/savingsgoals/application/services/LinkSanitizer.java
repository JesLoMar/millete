package com.puntomartinez.millete.savingsgoals.application.services;

import com.puntomartinez.millete.shared.domain.exception.InvalidInputException;

import java.net.URI;
import java.net.URISyntaxException;

public final class LinkSanitizer {

    private static final int MAX_LINK_LENGTH = 500;

    private LinkSanitizer() {
    }

    public static String sanitize(String link) {
        if (link == null || link.isBlank()) {
            return null;
        }

        String candidate = link.trim();

        if (!candidate.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            candidate = "https://" + candidate;
        }

        if (candidate.length() > MAX_LINK_LENGTH) {
            throw new InvalidInputException("El enlace no puede exceder 500 caracteres.");
        }

        try {
            URI uri = new URI(candidate);
            String scheme = uri.getScheme();
            boolean validScheme = scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
            if (!validScheme || uri.getHost() == null) {
                throw new InvalidInputException("El enlace debe ser una URL válida (http o https).");
            }
            return candidate;
        } catch (URISyntaxException e) {
            throw new InvalidInputException("El enlace debe ser una URL válida (http o https).");
        }
    }
}