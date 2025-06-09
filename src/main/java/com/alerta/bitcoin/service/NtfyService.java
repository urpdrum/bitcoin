package com.alerta.bitcoin.service;

import com.alerta.bitcoin.BitcoinApplication;
import com.alerta.bitcoin.model.TickerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class NtfyService {

    private static final Logger log = LoggerFactory.getLogger(NtfyService.class);
    private final WebClient webClient;
    private final String ntfyUrl;

    public NtfyService(@Value("${ntfy.url}") String ntfyUrl, WebClient.Builder webClientBuilder) {
        this.ntfyUrl = ntfyUrl;
        this.webClient = webClientBuilder.baseUrl(ntfyUrl).build();
    }

    // ESTE É O MÉTODO QUE ESTÁ FALTANDO OU COM ASSINATURA ERRADA!
    public void sendMessage(String title, String message, String tags) {
        log.info("Enviando notificação para NTFY: Título='{}', Mensagem='{}', Tags='{}'", title, message, tags);
        try {
            webClient.post()
                    .bodyValue(message)
                    .header("Title", title)
                    .header("Tags", tags)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Notificação NTFY enviada com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao enviar notificação NTFY: {}", e.getMessage(), e);
        }
    }
}