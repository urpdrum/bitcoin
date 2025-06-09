package com.alerta.bitcoin.client;

import com.alerta.bitcoin.model.TickerResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MercadoBitcoinApiClient {

    private final WebClient mercadoBitcoinPublicWebClient;

    // Injeção de dependência: O Spring vai fornecer a instância de WebClient que configuramos
    public MercadoBitcoinApiClient(@Qualifier("mercadoBitcoinPublicWebClient") WebClient mercadoBitcoinPublicWebClient) {
        this.mercadoBitcoinPublicWebClient = mercadoBitcoinPublicWebClient;
    }

    public Mono<TickerResponse> getTicker(String coin) {
        // Constrói a URL para a requisição (ex: /api/BTC/ticker/)
        // O WebClient é reativo, então ele retorna um Mono<TickerResponse>
        // Mono representa 0 ou 1 item, Flux representa 0 a N itens
        return mercadoBitcoinPublicWebClient.get()
                .uri("/{coin}/ticker/", coin) // Path da API para o ticker
                .retrieve() // Inicia a recuperação da resposta
                .bodyToMono(TickerResponse.class); // Mapeia o corpo da resposta para TickerResponse.class
    }
}