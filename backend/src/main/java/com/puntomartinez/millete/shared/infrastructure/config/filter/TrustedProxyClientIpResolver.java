package com.puntomartinez.millete.shared.infrastructure.config.filter;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Resuelve la IP del cliente usando X-Forwarded-For únicamente tras validar
 * la dirección de transporte contra los CIDR configurados explícitamente.
 */
public final class TrustedProxyClientIpResolver {
    private final List<Cidr> trustedProxies;

    public TrustedProxyClientIpResolver(String configuredCidrs) {
        this.trustedProxies = parseCidrs(configuredCidrs);
    }

    public String resolve(HttpServletRequest request) {
        String peer = normalize(request.getRemoteAddr());
        if (peer == null || trustedProxies.isEmpty()) {
            return peer == null ? request.getRemoteAddr() : peer;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return peer;
        }

        String[] values = forwarded.split(",", -1);
        String client = peer;
        for (int i = values.length - 1; i >= 0 && isTrusted(client); i--) {
            String hop = normalize(values[i].trim());
            if (hop == null) {
                return peer;
            }
            client = hop;
        }
        return client;
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String literal = value.trim();
        if (literal.indexOf(':') < 0) {
            String[] octets = literal.split("\\.", -1);
            if (octets.length != 4) {
                return null;
            }
            byte[] bytes = new byte[4];
            try {
                for (int i = 0; i < octets.length; i++) {
                    if (!octets[i].matches("[0-9]{1,3}")) {
                        return null;
                    }
                    int octet = Integer.parseInt(octets[i]);
                    if (octet > 255) {
                        return null;
                    }
                    bytes[i] = (byte) octet;
                }
                return InetAddress.getByAddress(bytes).getHostAddress();
            } catch (UnknownHostException exception) {
                return null;
            }
        }
        if (!literal.matches("[0-9a-fA-F:.]+")) {
            return null;
        }
        try {
            byte[] address = InetAddress.getByName(literal).getAddress();
            return InetAddress.getByAddress(address).getHostAddress().toLowerCase();
        } catch (UnknownHostException exception) {
            return null;
        }
    }

    private boolean isTrusted(String address) {
        for (Cidr cidr : trustedProxies) {
            if (cidr.contains(address)) {
                return true;
            }
        }
        return false;
    }

    private static List<Cidr> parseCidrs(String configured) {
        List<Cidr> result = new ArrayList<>();
        if (configured == null || configured.isBlank()) {
            return result;
        }
        for (String item : configured.split(",", -1)) {
            String value = item.trim();
            String[] parts = value.split("/", -1);
            String address = normalize(parts[0]);
            if (address == null || parts.length > 2) {
                throw new IllegalArgumentException("CIDR de proxy de confianza no válido");
            }
            byte[] bytes = addressBytes(address);
            int bits = bytes.length * 8;
            int prefix = parts.length == 1 ? bits : Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > bits) {
                throw new IllegalArgumentException("Prefijo CIDR de proxy de confianza no válido");
            }
            result.add(new Cidr(bytes, prefix));
        }
        return List.copyOf(result);
    }

    private static byte[] addressBytes(String address) {
        try {
            return InetAddress.getByName(address).getAddress();
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("Dirección IP no válida", exception);
        }
    }

    private record Cidr(byte[] network, int prefix) {
        private boolean contains(String address) {
            byte[] candidate = addressBytes(address);
            if (candidate.length != network.length) {
                return false;
            }
            int completeBytes = prefix / 8;
            int remainingBits = prefix % 8;
            for (int i = 0; i < completeBytes; i++) {
                if (candidate[i] != network[i]) {
                    return false;
                }
            }
            if (remainingBits == 0) {
                return true;
            }
            int mask = 0xff << (8 - remainingBits);
            return (candidate[completeBytes] & mask) == (network[completeBytes] & mask);
        }
    }
}
