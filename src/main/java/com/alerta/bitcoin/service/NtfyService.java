package com.alerta.bitcoin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class NtfyService {
    @Value("${ntfy.topic}")
    private String ntfyTopic;

    private static final String NTFY_BASE_URL = "https://ntfy.sh/";

    public void sendNotification(String message) {
        RestTemplate restTemplate = new RestTemplate();
        String url = NTFY_BASE_URL + ntfyTopic;
        restTemplate.postForObject(url, message, String.class);
        System.out.println("Notificação NTFY enviada: " + message);
    }
}
