package com.puntomartinez.millete.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuración del reloj de la aplicación.
 *
 * <p>En producción se usa {@link Clock#systemUTC()}. En tests se puede
 * sobreescribir este bean con un {@code Clock.fixed(...)} para obtener
 * determinismo.</p>
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}