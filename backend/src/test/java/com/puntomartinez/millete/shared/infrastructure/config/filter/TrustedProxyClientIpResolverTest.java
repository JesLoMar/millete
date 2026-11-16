package com.puntomartinez.millete.shared.infrastructure.config.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrustedProxyClientIpResolverTest {
    @Test
    void ignoresForwardingFromUntrustedPeerIncludingCloudflareHeaders() {
        HttpServletRequest request = request("203.0.113.20",
                "198.51.100.5, 192.0.2.10");
        when(request.getHeader("CF-Connecting-IP")).thenReturn("203.0.113.99");

        assertThat(new TrustedProxyClientIpResolver("192.0.2.0/24")
                .resolve(request)).isEqualTo("203.0.113.20");
    }

    @Test
    void walksForwardedChainFromNearestPeerAndDoesNotTrustSpoofedPrefix() {
        HttpServletRequest request = request("192.0.2.11",
                "198.51.100.7, 203.0.113.8, 192.0.2.10");

        assertThat(new TrustedProxyClientIpResolver("192.0.2.0/24")
                .resolve(request)).isEqualTo("203.0.113.8");
    }

    @Test
    void emptyConfigurationAndMalformedHeaderUseTransportPeer() {
        HttpServletRequest peer = request("192.0.2.20", "198.51.100.1");
        HttpServletRequest malformed = request("192.0.2.20",
                "198.51.100.1, attacker.example");

        assertThat(new TrustedProxyClientIpResolver("").resolve(peer))
                .isEqualTo("192.0.2.20");
        assertThat(new TrustedProxyClientIpResolver("192.0.2.0/24")
                .resolve(malformed)).isEqualTo("192.0.2.20");
    }

    @Test
    void normalizesIpv4AndIpv6Literals() {
        assertThat(TrustedProxyClientIpResolver.normalize("192.000.002.001"))
                .isEqualTo("192.0.2.1");
        assertThat(TrustedProxyClientIpResolver.normalize("2001:0db8:0:0:0:0:0:1"))
                .isEqualTo("2001:db8:0:0:0:0:0:1");
    }

    private HttpServletRequest request(String peer, String forwarded) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(peer);
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwarded);
        return request;
    }
}
