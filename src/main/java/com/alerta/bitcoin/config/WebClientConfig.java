package com.alerta.bitcoin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${mercado-bitcoin.api.base-url.public}")
    private String mercadoBitcoinPublicApiBaseUrl;

    @Value("${mercado-bitcoin.api.base-url.private}") // Adicione esta linha
    private String mercadoBitcoinPrivateApiBaseUrl;   // Adicione esta linha

    @Bean
    public WebClient mercadoBitcoinPublicWebClient() {
        return WebClient.builder()
                .baseUrl(mercadoBitcoinPublicApiBaseUrl)
                .build();
    }

    @Bean // NOVO BEAN PARA A API PRIVADA
    public WebClient mercadoBitcoinPrivateWebClient() {
        return WebClient.builder()
                .baseUrl(mercadoBitcoinPrivateApiBaseUrl)
                .build();
    }
}
