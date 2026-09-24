package com.juanfran.fixflow.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Reloj inyectable: en los tests se sustituye por uno fijo para que las fechas sean predecibles. */
@Configuration
public class RelojConfig {

    @Bean
    Clock reloj() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }
}
