package com.alerta.bitcoin.service;



import com.alerta.bitcoin.dto.BitcoinPriceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct; // Para Spring Boot 3+
// import javax.annotation.PostConstruct; // Para Spring Boot 2.x

    @Service
    public class BitcoinMonitorService {

        private static final Logger logger = LoggerFactory.getLogger(BitcoinMonitorService.class);
        private final RestTemplate restTemplate;
        private final NotificationService notificationService;

        @Value("${coingecko.api.url:https://api.coingecko.com/api/v3/simple/price}")
        private String coingeckoApiUrl;

        @Value("${bitcoin.currency:usd}") // Moeda para monitorar (usd, brl, eur)
        private String currency;

        @Value("${alert.drop.percentage:0.30}") // Alerta se cair 30% (0.30)
        private double alertDropPercentage;

        private Double initialPrice = null;
        private boolean alertSentForCurrentDrop = false;

        @Autowired
        public BitcoinMonitorService(RestTemplate restTemplate, NotificationService notificationService) {
            this.restTemplate = restTemplate;
            this.notificationService = notificationService;
        }

        @PostConstruct
        public void initializeReferencePrice() {
            fetchAndSetInitialPrice();
        }

        public void fetchAndSetInitialPrice() {
            logger.info("Tentando definir o preço de referência inicial do Bitcoin em {}...", currency.toUpperCase());
            Double currentPrice = fetchCurrentBitcoinPrice();
            if (currentPrice != null) {
                this.initialPrice = currentPrice;
                this.alertSentForCurrentDrop = false; // Reseta o alerta ao definir novo preço inicial
                logger.info("Preço de referência inicial do Bitcoin ({}) definido para: {} {}",
                        currency.toUpperCase(), String.format("%.2f", this.initialPrice), currency.toUpperCase());
            } else {
                logger.warn("Não foi possível buscar o preço inicial do Bitcoin. Tentando novamente na próxima verificação.");
            }
        }

        public Double fetchCurrentBitcoinPrice() {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(coingeckoApiUrl)
                    .queryParam("ids", "bitcoin")
                    .queryParam("vs_currencies", currency.toLowerCase());

            try {
                BitcoinPriceResponse response = restTemplate.getForObject(builder.toUriString(), BitcoinPriceResponse.class);
                if (response != null) {
                    Double price = response.getPriceInCurrency(currency);
                    if (price != null) {
                        return price;
                    } else {
                        logger.warn("Resposta da API não continha o preço para a moeda: {}", currency);
                    }
                } else {
                    logger.warn("Resposta da API foi nula.");
                }
            } catch (Exception e) {
                logger.error("Erro ao buscar preço do Bitcoin: {}", e.getMessage(), e); // Adiciona stack trace
            }
            return null;
        }

        @Scheduled(fixedRateString = "${schedule.fixed.rate:60000}") // Verifica a cada 60 segundos (configurável)
        public void checkBitcoinPriceAndAlert() {
            if (this.initialPrice == null) {
                logger.info("Preço de referência ainda não definido. Tentando buscar...");
                fetchAndSetInitialPrice();
                if (this.initialPrice == null) {
                    logger.warn("Ainda não foi possível buscar o preço de referência. Verificação adiada.");
                    return;
                }
            }

            Double currentPrice = fetchCurrentBitcoinPrice();
            if (currentPrice == null) {
                logger.warn("Não foi possível buscar o preço atual do Bitcoin para verificação.");
                return;
            }

            String currentPriceFormatted = String.format("%.2f", currentPrice);
            String initialPriceFormatted = String.format("%.2f", this.initialPrice);

            logger.info("Preço atual BTC/{}: {}. (Referência: {} {})",
                    currency.toUpperCase(), currentPriceFormatted, initialPriceFormatted, currency.toUpperCase());

            double thresholdPrice = this.initialPrice * (1 - alertDropPercentage);
            String thresholdPriceFormatted = String.format("%.2f", thresholdPrice);

            if (currentPrice < thresholdPrice) {
                if (!alertSentForCurrentDrop) {
                    String title = String.format("ALERTA BITCOIN CAIU >%.0f%%!", alertDropPercentage * 100);
                    String message = String.format("BTC caiu para %s %s (de %s %s). Limite: %s %s.",
                            currentPriceFormatted, currency.toUpperCase(),
                            initialPriceFormatted, currency.toUpperCase(),
                            thresholdPriceFormatted, currency.toUpperCase());

                    notificationService.sendAlert(title, message);
                    alertSentForCurrentDrop = true;
                    // Opcional: você pode querer resetar o initialPrice aqui para um novo patamar ou aguardar manualmente.
                    // this.initialPrice = currentPrice; // Se quiser rebasear imediatamente após o alerta.
                } else {
                    logger.info("Preço do Bitcoin ({} {}) ainda abaixo do limite de alerta ({}), mas alerta já foi enviado.",
                            currentPriceFormatted, currency.toUpperCase(), thresholdPriceFormatted);
                }
            } else if (currentPrice > this.initialPrice && alertSentForCurrentDrop) {
                // Se o preço se recuperar e subir ACIMA do preço inicial que disparou o alerta,
                // podemos resetar o flag para futuros alertas se cair novamente.
                logger.info("Preço do Bitcoin ({} {}) recuperou acima do preço de referência inicial ({} {}). Resetando flag de alerta.",
                        currentPriceFormatted, currency.toUpperCase(), initialPriceFormatted, currency.toUpperCase());
                alertSentForCurrentDrop = false;
                // Opcional: redefinir o preço inicial para o novo pico se desejar
                // this.initialPrice = currentPrice;
            }
        }
    }

 //====================
//package com.alerta.bitcoin.service; // Ou o seu pacote
//
//import com.alerta.bitcoin.dto.BitcoinPriceResponse;
//import com.alerta.bitcoin.service.NotificationService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity; // Adicionar esta importação
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException; // Adicionar
//import org.springframework.web.client.HttpServerErrorException; // Adicionar
//import org.springframework.web.client.ResourceAccessException; // Adicionar
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import jakarta.annotation.PostConstruct;
//
//@Service
//public class BitcoinMonitorService {
//
//    private static final Logger logger = LoggerFactory.getLogger(BitcoinMonitorService.class);
//    private final RestTemplate restTemplate;
//    private final NotificationService notificationService;
//
//    @Value("${coingecko.api.url:https://api.coingecko.com/api/v3/simple/price}")
//    private String coingeckoApiUrl;
//
//    @Value("${bitcoin.currency:usd}")
//    private String currency;
//
//    @Value("${alert.drop.percentage:0.30}")
//    private double alertDropPercentage;
//
//    private Double initialPrice = null;
//    private boolean alertSentForCurrentDrop = false;
//
//    @Autowired
//    public BitcoinMonitorService(RestTemplate restTemplate, NotificationService notificationService) {
//        this.restTemplate = restTemplate;
//        this.notificationService = notificationService;
//    }
//
//    @PostConstruct
//    public void initializeReferencePrice() {
//        fetchAndSetInitialPrice();
//    }
//
//    public void fetchAndSetInitialPrice() {
//        logger.info("Tentando definir o preço de referência inicial do Bitcoin em {}...", currency.toUpperCase());
//        Double currentPrice = fetchCurrentBitcoinPrice();
//        if (currentPrice != null) {
//            this.initialPrice = currentPrice;
//            this.alertSentForCurrentDrop = false;
//            logger.info("Preço de referência inicial do Bitcoin ({}) definido para: {} {}",
//                    currency.toUpperCase(), String.format("%.2f", this.initialPrice), currency.toUpperCase());
//        } else {
//            logger.warn("Não foi possível buscar o preço inicial do Bitcoin. Tentando novamente na próxima verificação.");
//        }
//    }
//
//    public Double fetchCurrentBitcoinPrice() {
//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(coingeckoApiUrl)
//                .queryParam("ids", "bitcoin")
//                .queryParam("vs_currencies", currency.toLowerCase());
//
//        String apiUrl = builder.toUriString(); // URL completa
//        logger.debug("Chamando API CoinGecko: {}", apiUrl); // Log da URL
//
//        try {
//            // Usar getForEntity para obter mais detalhes da resposta, incluindo o corpo como String
//            ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
//            String rawResponse = responseEntity.getBody();
//            logger.debug("Resposta bruta da API CoinGecko: {}", rawResponse);
//
//            // Agora, desserializar a string bruta para o nosso objeto
//            // É preciso um ObjectMapper para isso, ou reconfigurar RestTemplate para logar antes de desserializar.
//            // Por simplicidade, vamos tentar desserializar diretamente e se falhar, o log acima ajudará.
//
//            BitcoinPriceResponse response = restTemplate.getForObject(apiUrl, BitcoinPriceResponse.class);
//
//            if (response != null) {
//                Double price = response.getPriceInCurrency(currency); // Usa toLowerCase internamente
//                if (price != null) {
//                    logger.debug("Preço extraído para {}: {}", currency.toUpperCase(), price);
//                    return price;
//                } else {
//                    logger.warn("O objeto BitcoinPriceResponse foi obtido, mas não continha o preço para a moeda: {}. Resposta mapeada: {}", currency, response.getBitcoin());
//                }
//            } else {
//                logger.warn("Resposta da API (objeto BitcoinPriceResponse) foi nula após desserialização.");
//            }
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            logger.error("Erro HTTP ao buscar preço do Bitcoin: {} - {} \nCorpo da Resposta: {}", e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString(), e);
//        } catch (ResourceAccessException e) {
//            logger.error("Erro de acesso ao recurso (rede/timeout) ao buscar preço do Bitcoin: {}", e.getMessage(), e);
//        } catch (Exception e) {
//            logger.error("Erro inesperado ao buscar preço do Bitcoin: {}", e.getMessage(), e);
//        }
//        return null;
//    }
//
//    // ... (resto da classe checkBitcoinPriceAndAlert)
//    @Scheduled(fixedRateString = "${schedule.fixed.rate:60000}")
//    public void checkBitcoinPriceAndAlert() {
//        if (this.initialPrice == null) {
//            logger.info("Preço de referência ainda não definido. Tentando buscar...");
//            fetchAndSetInitialPrice();
//            if (this.initialPrice == null) {
//                logger.warn("Ainda não foi possível buscar o preço de referência. Verificação adiada.");
//                return;
//            }
//        }
//
//        Double currentPrice = fetchCurrentBitcoinPrice();
//        if (currentPrice == null) {
//            logger.warn("Não foi possível buscar o preço atual do Bitcoin para verificação.");
//            return;
//        }
//
//        String currentPriceFormatted = String.format("%.2f", currentPrice);
//        String initialPriceFormatted = String.format("%.2f", this.initialPrice);
//
//        logger.info("Preço atual BTC/{}: {}. (Referência: {} {})",
//                currency.toUpperCase(), currentPriceFormatted, initialPriceFormatted, currency.toUpperCase());
//
//        double thresholdPrice = this.initialPrice * (1 - alertDropPercentage);
//        String thresholdPriceFormatted = String.format("%.2f", thresholdPrice);
//
//        if (currentPrice < thresholdPrice) {
//            if (!alertSentForCurrentDrop) {
//                String title = String.format("ALERTA BITCOIN CAIU >%.0f%%!", alertDropPercentage * 100);
//                String message = String.format("BTC caiu para %s %s (de %s %s). Limite: %s %s.",
//                        currentPriceFormatted, currency.toUpperCase(),
//                        initialPriceFormatted, currency.toUpperCase(),
//                        thresholdPriceFormatted, currency.toUpperCase());
//
//
//                logger.info("Condição de alerta atendida. Chamando NotificationService...");
//                notificationService.sendAlert(title, message);
//                alertSentForCurrentDrop = true;
//            } else {
//                logger.info("Preço do Bitcoin ({} {}) ainda abaixo do limite de alerta ({}), mas alerta já foi enviado.",
//                        currentPriceFormatted, currency.toUpperCase(), thresholdPriceFormatted);
//            }
//        } else if (currentPrice > this.initialPrice && alertSentForCurrentDrop) {
//            logger.info("Preço do Bitcoin ({} {}) recuperou acima do preço de referência inicial ({} {}). Resetando flag de alerta.",
//                    currentPriceFormatted, currency.toUpperCase(), initialPriceFormatted, currency.toUpperCase());
//            alertSentForCurrentDrop = false;
//        }
//    }
//}
