package com.alerta.bitcoin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${mercado-bitcoin.api.base-url.public}")
    private String mercadoBitcoinPublicApiBaseUrl;

    @Bean // O Spring vai criar e gerenciar uma instância de WebClient
    public WebClient mercadoBitcoinPublicWebClient() {
        return WebClient.builder()
                .baseUrl(mercadoBitcoinPublicApiBaseUrl) // URL base para todas as chamadas
                .build();
    }
}
