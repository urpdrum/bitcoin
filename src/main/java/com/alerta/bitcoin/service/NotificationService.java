package com.alerta.bitcoin.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*; // Para SystemTray
import java.net.URI;

    @Service
    public class NotificationService {

        private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
        private final RestTemplate restTemplate;

        // Configurável via application.properties
        @Value("${ntfy.topic:bitcoin-price-alerts-meutopico}") // Exemplo de tópico, mude para um único seu
        private String ntfyTopic;

        @Value("${ntfy.enabled:true}")
        private boolean ntfyEnabled;

        @Value("${desktop.notification.enabled:true}")
        private boolean desktopNotificationEnabled;

        public NotificationService(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        public void sendAlert(String title, String message) {
            logger.warn("ALERTA: {} - {}", title, message); // Sempre loga no console

            if (desktopNotificationEnabled) {
                sendDesktopNotification(title, message);
            }

            if (ntfyEnabled && ntfyTopic != null && !ntfyTopic.isEmpty()) {
                sendNtfyNotification(title, message);
            }
        }

        private void sendDesktopNotification(String title, String message) {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png"); // Você precisaria de um 'icon.png'
                // ou use um ícone padrão.
                // Pode ser null se não tiver imagem.

                // Tenta carregar uma imagem de ícone (opcional)
                // Image image = null;
                // try {
                //    URL imageUrl = getClass().getResource("/static/bitcoin_icon.png"); // Coloque um ícone em src/main/resources/static
                //    if (imageUrl != null) {
                //        image = Toolkit.getDefaultToolkit().getImage(imageUrl);
                //    }
                // } catch (Exception e) {
                //    logger.warn("Não foi possível carregar o ícone de notificação: {}", e.getMessage());
                // }


                TrayIcon trayIcon = new TrayIcon(image, "Bitcoin Alerta"); // 'image' pode ser null
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("Alerta de Preço do Bitcoin");

                try {
                    // Adicionar o ícone à bandeja pode não ser necessário só para exibir um balão,
                    // mas algumas plataformas podem exigir.
                    // tray.add(trayIcon); // Descomente se necessário, pode causar problemas se já adicionado.

                    trayIcon.displayMessage(title, message, TrayIcon.MessageType.WARNING);
                    logger.info("Notificação de desktop enviada.");

                    // Remover o ícone depois de um tempo para não poluir a bandeja (opcional)
                    // new Timer().schedule(new TimerTask() {
                    //     @Override
                    //     public void run() {
                    //         tray.remove(trayIcon);
                    //     }
                    // }, 10000); // Remove após 10 segundos


                } catch (Exception e) { // AWTException se não houver SystemTray
                    logger.error("Erro ao enviar notificação de desktop: {}", e.getMessage());
                }
            } else {
                logger.warn("SystemTray não é suportado. Notificação de desktop desabilitada.");
            }
        }

        private void sendNtfyNotification(String title, String message) {
            try {
                String ntfyUrl = "https://ntfy.sh/" + ntfyTopic;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN); // ntfy espera texto puro
                headers.set("Title", title); // Título via header
                // Você pode adicionar tags, prioridade, etc., conforme a documentação do ntfy.sh
                // headers.set("Tags", "warning,bitcoin");
                // headers.set("Priority", "high"); // ou "urgent"

                HttpEntity<String> entity = new HttpEntity<>(message, headers);
                restTemplate.exchange(URI.create(ntfyUrl), HttpMethod.POST, entity, String.class);
                logger.info("Notificação enviada para ntfy.sh tópico: {}", ntfyTopic);
            } catch (Exception e) {
                logger.error("Erro ao enviar notificação para ntfy.sh: {}", e.getMessage());
            }
        }
    }

