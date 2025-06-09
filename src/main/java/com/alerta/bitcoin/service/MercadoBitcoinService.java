package com.alerta.bitcoin.service;

//import com.alerta.bitcoin.dto.MercadoBitcoinTickerResponse;
import com.alerta.bitcoin.client.MercadoBitcoinApiClient;
import com.alerta.bitcoin.model.TickerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MercadoBitcoinService {

    private static final Logger log = LoggerFactory.getLogger(MercadoBitcoinService.class);
    private final MercadoBitcoinApiClient apiClient;

    public MercadoBitcoinService(MercadoBitcoinApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public TickerResponse getCurrentTicker(String coin) {
        log.info("Buscando ticker para a moeda: {}", coin);
        try {
            // .block() é usado aqui para simplificar para iniciantes.
            // Em apps reativas mais complexas, você evitaria .block() e trabalharia com o Mono diretamente.
            // Para testar a API, é aceitável.
            TickerResponse response = apiClient.getTicker(coin).block();
            if (response != null && response.ticker() != null) {
                log.info("Ticker de {} recebido: Last={}, Buy={}, Sell={}",
                        coin, response.ticker().last(), response.ticker().buy(), response.ticker().sell());
                return response;
            } else {
                log.warn("Nenhuma resposta de ticker válida recebida para {}", coin);
                return null;
            }
        } catch (Exception e) {
            log.error("Erro ao buscar ticker para {}: {}", coin, e.getMessage());
            return null;
        }
    }
}
