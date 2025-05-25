//package com.alerta.bitcoin.service;
//
//
//
//import com.alerta.bitcoin.dto.BitcoinPriceResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import jakarta.annotation.PostConstruct; // Para Spring Boot 3+
//// import javax.annotation.PostConstruct; // Para Spring Boot 2.x
//
//    @Service
//    public class BitcoinMonitorService {
//
//        private static final Logger logger = LoggerFactory.getLogger(BitcoinMonitorService.class);
//        private final RestTemplate restTemplate;
//        private final NotificationService notificationService;
//
//        @Value("${coingecko.api.url:https://api.coingecko.com/api/v3/simple/price}")
//        private String coingeckoApiUrl;
//
//        @Value("${bitcoin.currency:usd}") // Moeda para monitorar (usd, brl, eur)
//        private String currency;
//
//        @Value("${alert.drop.percentage:0.02}") // Alerta se cair 30% (0.30)
//        private double alertDropPercentage;
//
//        private Double initialPrice = null;
//        private boolean alertSentForCurrentDrop = false;
//
//        @Autowired
//        public BitcoinMonitorService(RestTemplate restTemplate, NotificationService notificationService) {
//            this.restTemplate = restTemplate;
//            this.notificationService = notificationService;
//        }
//
//        @PostConstruct
//        public void initializeReferencePrice() {
//            fetchAndSetInitialPrice();
//        }
//
//        public void fetchAndSetInitialPrice() {
//            logger.info("Tentando definir o preço de referência inicial do Bitcoin em {}...", currency.toUpperCase());
//            Double currentPrice = fetchCurrentBitcoinPrice();
//            if (currentPrice != null) {
//                this.initialPrice = currentPrice;
//                this.alertSentForCurrentDrop = false; // Reseta o alerta ao definir novo preço inicial
//                logger.info("Preço de referência inicial do Bitcoin ({}) definido para: {} {}",
//                        currency.toUpperCase(), String.format("%.2f", this.initialPrice), currency.toUpperCase());
//            } else {
//                logger.warn("Não foi possível buscar o preço inicial do Bitcoin. Tentando novamente na próxima verificação.");
//            }
//        }
//
//        public Double fetchCurrentBitcoinPrice() {
//            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(coingeckoApiUrl)
//                    .queryParam("ids", "bitcoin")
//                    .queryParam("vs_currencies", currency.toLowerCase());
//
//            try {
//                BitcoinPriceResponse response = restTemplate.getForObject(builder.toUriString(), BitcoinPriceResponse.class);
//                if (response != null) {
//                    Double price = response.getPriceInCurrency(currency);
//                    if (price != null) {
//                        return price;
//                    } else {
//                        logger.warn("Resposta da API não continha o preço para a moeda: {}", currency);
//                    }
//                } else {
//                    logger.warn("Resposta da API foi nula.");
//                }
//            } catch (Exception e) {
//                logger.error("Erro ao buscar preço do Bitcoin: {}", e.getMessage(), e); // Adiciona stack trace
//            }
//            return null;
//        }
//
//        @Scheduled(fixedRateString = "${schedule.fixed.rate:60000}") // Verifica a cada 60 segundos (configurável)
//        public void checkBitcoinPriceAndAlert() {
//            if (this.initialPrice == null) {
//                logger.info("Preço de referência ainda não definido. Tentando buscar...");
//                fetchAndSetInitialPrice();
//                if (this.initialPrice == null) {
//                    logger.warn("Ainda não foi possível buscar o preço de referência. Verificação adiada.");
//                    return;
//                }
//            }
//
//            Double currentPrice = fetchCurrentBitcoinPrice();
//            if (currentPrice == null) {
//                logger.warn("Não foi possível buscar o preço atual do Bitcoin para verificação.");
//                return;
//            }
//
//            String currentPriceFormatted = String.format("%.2f", currentPrice);
//            String initialPriceFormatted = String.format("%.2f", this.initialPrice);
//
//            logger.info("Preço atual BTC/{}: {}. (Referência: {} {})",
//                    currency.toUpperCase(), currentPriceFormatted, initialPriceFormatted, currency.toUpperCase());
//
//            double thresholdPrice = this.initialPrice * (1 - alertDropPercentage);
//            String thresholdPriceFormatted = String.format("%.2f", thresholdPrice);
//
//            if (currentPrice < thresholdPrice) {
//                if (!alertSentForCurrentDrop) {
//                    String title = String.format("ALERTA BITCOIN CAIU >%.0f%%!", alertDropPercentage * 100);
//                    String message = String.format("BTC caiu para %s %s (de %s %s). Limite: %s %s.",
//                            currentPriceFormatted, currency.toUpperCase(),
//                            initialPriceFormatted, currency.toUpperCase(),
//                            thresholdPriceFormatted, currency.toUpperCase());
//
//                    notificationService.sendAlert(title, message);
//                    alertSentForCurrentDrop = true;
//                    // Opcional: você pode querer resetar o initialPrice aqui para um novo patamar ou aguardar manualmente.
//                    // this.initialPrice = currentPrice; // Se quiser rebasear imediatamente após o alerta.
//                } else {
//                    logger.info("Preço do Bitcoin ({} {}) ainda abaixo do limite de alerta ({}), mas alerta já foi enviado.",
//                            currentPriceFormatted, currency.toUpperCase(), thresholdPriceFormatted);
//                }
//            } else if (currentPrice > this.initialPrice && alertSentForCurrentDrop) {
//                // Se o preço se recuperar e subir ACIMA do preço inicial que disparou o alerta,
//                // podemos resetar o flag para futuros alertas se cair novamente.
//                logger.info("Preço do Bitcoin ({} {}) recuperou acima do preço de referência inicial ({} {}). Resetando flag de alerta.",
//                        currentPriceFormatted, currency.toUpperCase(), initialPriceFormatted, currency.toUpperCase());
//                alertSentForCurrentDrop = false;
//                // Opcional: redefinir o preço inicial para o novo pico se desejar
//                // this.initialPrice = currentPrice;
//            }
//        }
//    }

 //====================

 //c[odigo 22222
//        package com.alerta.bitcoin.service; // Ou o seu pacote
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
//        // ********************************************************************
////// ** BLOCO DE TESTE TEMPORÁRIO - REMOVER DEPOIS **
////        if (this.initialPrice != null && Math.abs(this.initialPrice - currentPrice) < 10) { // Se o preço mudou pouco
////            logger.warn("!!! MODO DE TESTE: FORÇANDO QUEDA DE PREÇO PARA TESTAR ALERTA !!!");
////            currentPrice = this.initialPrice * 0.98; // Força uma queda de 2% (que é > 1% do seu teste)
////        }
////===============================================================
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





//// c[odigo 33333333333


// Ou o seu pacote
package com.alerta.bitcoin.service;
import com.alerta.bitcoin.dto.BitcoinPriceResponse;
import com.alerta.bitcoin.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;

@Service
public class BitcoinMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(BitcoinMonitorService.class);
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;

    @Value("${coingecko.api.url:https://api.coingecko.com/api/v3/simple/price}")
    private String coingeckoApiUrl;

    @Value("${bitcoin.currency:brl}")
    private String currency;

    // Alertas de Queda
    @Value("${alert.drop.percentage:0.01}") // Alerta se cair 30% (0.30)
    private double alertDropPercentage;
    private boolean alertSentForCurrentDrop = false;

    // Alertas de Aumento (NOVAS PROPRIEDADES)
    @Value("${alert.rise.percentage:0.01}") // Alerta se subir 20% (0.20)
    private double alertRisePercentage;
    private boolean alertSentForCurrentRise = false;


    private Double initialPrice = null;


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
            this.alertSentForCurrentDrop = false; // Reseta o alerta de queda
            this.alertSentForCurrentRise = false; // Reseta o alerta de aumento
            logger.info("Preço de referência inicial do Bitcoin ({}) definido para: {} {}",
                    currency.toUpperCase(), String.format("%.2f", this.initialPrice), currency.toUpperCase());
        } else {
            logger.warn("Não foi possível buscar o preço inicial do Bitcoin. Tentando novamente na próxima verificação.");
        }
    }

    public Double fetchCurrentBitcoinPrice() {
        // ... (método fetchCurrentBitcoinPrice permanece o mesmo, com os logs de depuração que adicionamos)
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(coingeckoApiUrl)
                .queryParam("ids", "bitcoin")
                .queryParam("vs_currencies", currency.toLowerCase());

        String apiUrl = builder.toUriString();
        logger.debug("Chamando API CoinGecko: {}", apiUrl);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
            String rawResponse = responseEntity.getBody();
            logger.debug("Resposta bruta da API CoinGecko: {}", rawResponse);

            BitcoinPriceResponse response = restTemplate.getForObject(apiUrl, BitcoinPriceResponse.class);

            if (response != null) {
                Double price = response.getPriceInCurrency(currency);
                if (price != null) {
                    logger.debug("Preço extraído para {}: {}", currency.toUpperCase(), price);
                    return price;
                } else {
                    logger.warn("O objeto BitcoinPriceResponse foi obtido, mas não continha o preço para a moeda: {}. Resposta mapeada: {}", currency, response.getBitcoin());
                }
            } else {
                logger.warn("Resposta da API (objeto BitcoinPriceResponse) foi nula após desserialização.");
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Erro HTTP ao buscar preço do Bitcoin: {} - {} \nCorpo da Resposta: {}", e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            logger.error("Erro de acesso ao recurso (rede/timeout) ao buscar preço do Bitcoin: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar preço do Bitcoin: {}", e.getMessage(), e);
        }
        return null;
    }


    @Scheduled(fixedRateString = "${schedule.fixed.rate:60000}")
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
//        // ********************************************************************
//        // ************************Testes de Alerta -- altere o aplications.propierts para 0.01*********AUMENTO******************************************
//// ** BLOCO DE TESTE TEMPORÁRIO PARA AUMENTO DE PREÇO - REMOVER DEPOIS **
//// Certifique-se que alert.rise.percentage está configurado para um valor pequeno
//// como 0.01 no application.properties para este teste.
//        if (this.initialPrice != null && Math.abs(this.initialPrice - currentPrice) < (this.initialPrice * 0.005)) { // Se o preço mudou muito pouco (menos de 0.5%)
//            logger.warn("!!! MODO DE TESTE: FORÇANDO AUMENTO DE PREÇO PARA TESTAR ALERTA DE ALTA !!!");
//            currentPrice = this.initialPrice * 1.02; // Força um aumento de 2% (que deve ser > que o seu alert.rise.percentage de teste)
//        }
////// ***************************************************************************
////        // ****************Testes de Alerta***************************Baxa NO PREÇO*************************
////// ** BLOCO DE TESTE TEMPORÁRIO - REMOVER DEPOIS **
//       if  (this.initialPrice != null && Math.abs(this.initialPrice - currentPrice) < 10) { // Se o preço mudou pouco
//            logger.warn("!!! MODO DE TESTE: FORÇANDO QUEDA DE PREÇO PARA TESTAR ALERTA !!!");
//            currentPrice = this.initialPrice * 0.98; // Força uma queda de 2% (que é > 1% do seu teste)
//        }
//// ********************************************************************


        String currentPriceFormatted = String.format("%.2f", currentPrice);
        String initialPriceFormatted = String.format("%.2f", this.initialPrice);

        logger.info("Preço atual BTC/{}: {}. (Referência: {} {})",
                currency.toUpperCase(), currentPriceFormatted, initialPriceFormatted, currency.toUpperCase());

        // --- Lógica de Alerta de Queda ---
        double dropThresholdPrice = this.initialPrice * (1 - alertDropPercentage);
        String dropThresholdPriceFormatted = String.format("%.2f", dropThresholdPrice);

        logger.debug("Comparando para alerta de QUEDA: currentPrice = {}, dropThresholdPrice = {}, alertSentForCurrentDrop = {}",
                currentPriceFormatted, dropThresholdPriceFormatted, alertSentForCurrentDrop);

        if (currentPrice < dropThresholdPrice) {
            logger.debug("QUEDA: CONDIÇÃO (currentPrice < dropThresholdPrice) É VERDADEIRA");
            if (!alertSentForCurrentDrop) {
                logger.debug("QUEDA: CONDIÇÃO (!alertSentForCurrentDrop) É VERDADEIRA. Enviando alerta de queda.");
                String title = String.format("ALERTA BITCOIN CAIU >%.0f%%!", alertDropPercentage * 100);
                String message = String.format("BTC caiu para %s %s (de %s %s). Limite de queda: %s %s.",
                        currentPriceFormatted, currency.toUpperCase(),
                        initialPriceFormatted, currency.toUpperCase(),
                        dropThresholdPriceFormatted, currency.toUpperCase());

                logger.info("Condição de alerta de QUEDA atendida. Chamando NotificationService...");
                notificationService.sendAlert(title, message);
                alertSentForCurrentDrop = true;
                alertSentForCurrentRise = false; // Se caiu, não pode estar em alta em relação ao mesmo initialPrice
            } else {
                logger.info("QUEDA: Preço do Bitcoin ainda abaixo do limite ({}), mas alerta de queda já foi enviado.", dropThresholdPriceFormatted);
            }
        } else if (currentPrice > this.initialPrice && alertSentForCurrentDrop) {
            // Se o preço se recuperar e subir ACIMA do preço inicial que disparou o alerta de queda,
            // podemos resetar o flag para futuros alertas se cair novamente.
            logger.info("QUEDA: Preço do Bitcoin recuperou acima do preço de referência inicial ({} {}). Resetando flag de alerta de queda.",
                    initialPriceFormatted, currency.toUpperCase());
            alertSentForCurrentDrop = false;
        }

        // --- Lógica de Alerta de Aumento (NOVA) ---
        double riseThresholdPrice = this.initialPrice * (1 + alertRisePercentage);
        String riseThresholdPriceFormatted = String.format("%.2f", riseThresholdPrice);

        logger.debug("Comparando para alerta de AUMENTO: currentPrice = {}, riseThresholdPrice = {}, alertSentForCurrentRise = {}",
                currentPriceFormatted, riseThresholdPriceFormatted, alertSentForCurrentRise);

        if (currentPrice > riseThresholdPrice) {
            logger.debug("AUMENTO: CONDIÇÃO (currentPrice > riseThresholdPrice) É VERDADEIRA");
            if (!alertSentForCurrentRise) {
                logger.debug("AUMENTO: CONDIÇÃO (!alertSentForCurrentRise) É VERDADEIRA. Enviando alerta de aumento.");
                String title = String.format("ALERTA BITCOIN SUBIU >%.0f%%!", alertRisePercentage * 100);
                String message = String.format("BTC subiu para %s %s (de %s %s). Limite de aumento: %s %s.",
                        currentPriceFormatted, currency.toUpperCase(),
                        initialPriceFormatted, currency.toUpperCase(),
                        riseThresholdPriceFormatted, currency.toUpperCase());

                logger.info("Condição de alerta de AUMENTO atendida. Chamando NotificationService...");
                notificationService.sendAlert(title, message);
                alertSentForCurrentRise = true;
                alertSentForCurrentDrop = false; // Se subiu, não pode estar em queda em relação ao mesmo initialPrice
            } else {
                logger.info("AUMENTO: Preço do Bitcoin ainda acima do limite ({}), mas alerta de aumento já foi enviado.", riseThresholdPriceFormatted);
            }
        } else if (currentPrice < this.initialPrice && alertSentForCurrentRise) {
            // Se o preço cair ABAIXO do preço inicial que disparou o alerta de aumento,
            // podemos resetar o flag para futuros alertas se subir novamente.
            logger.info("AUMENTO: Preço do Bitcoin caiu abaixo do preço de referência inicial ({} {}). Resetando flag de alerta de aumento.",
                    initialPriceFormatted, currency.toUpperCase());
            alertSentForCurrentRise = false;
        }
    }
}