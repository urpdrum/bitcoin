package com.alerta.bitcoin.client;

import com.alerta.bitcoin.model.privateapi.AccountInfoResponse;
import com.alerta.bitcoin.model.privateapi.PlaceOrderResponse;
import com.alerta.bitcoin.security.HmacSha512Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
@Component
public class MercadoBitcoinPrivateApiClient {
    private static final Logger log = LoggerFactory.getLogger(MercadoBitcoinPrivateApiClient.class);

    private final WebClient mercadoBitcoinPrivateWebClient;
    private final String clientId;
    private final String secretKey;

    public MercadoBitcoinPrivateApiClient(
            @Qualifier("mercadoBitcoinPrivateWebClient") WebClient mercadoBitcoinPrivateWebClient,
            @Value("${mercado-bitcoin.api.client-id}") String clientId,
            @Value("${mercado-bitcoin.api.secret-key}") String secretKey) {
        this.mercadoBitcoinPrivateWebClient = mercadoBitcoinPrivateWebClient;
        this.clientId = clientId;
        this.secretKey = secretKey;
    }

    // Método para obter informações da conta (saldo)
    public AccountInfoResponse getAccountInfo() {
        log.info("Buscando informações da conta no Mercado Bitcoin...");
        Map<String, String> params = new TreeMap<>(); // TreeMap para garantir ordem para assinatura
        params.put("tapi_method", "get_account_info");
        params.put("tapi_nonce", String.valueOf(System.currentTimeMillis())); // Nonce simples: timestamp em millis

        return postPrivateApiRequest(params, AccountInfoResponse.class);
    }

    // Método para realizar uma ordem de compra
    public PlaceOrderResponse placeBuyOrder(String coinPair, BigDecimal quantity, BigDecimal limitPrice) {
        log.info("Colocando ordem de COMPRA: {} {} por R${}", quantity, coinPair, limitPrice);
        Map<String, String> params = new TreeMap<>();
        params.put("tapi_method", "place_buy_order");
        params.put("tapi_nonce", String.valueOf(System.currentTimeMillis()));
        params.put("coin_pair", coinPair);
        params.put("quantity", quantity.toPlainString()); // Importante para BigDecimal
        params.put("limit_price", limitPrice.toPlainString()); // Importante para BigDecimal

        return postPrivateApiRequest(params, PlaceOrderResponse.class);
    }

    // Método para realizar uma ordem de venda
    public PlaceOrderResponse placeSellOrder(String coinPair, BigDecimal quantity, BigDecimal limitPrice) {
        log.info("Colocando ordem de VENDA: {} {} por R${}", quantity, coinPair, limitPrice);
        Map<String, String> params = new TreeMap<>();
        params.put("tapi_method", "place_sell_order");
        params.put("tapi_nonce", String.valueOf(System.currentTimeMillis()));
        params.put("coin_pair", coinPair);
        params.put("quantity", quantity.toPlainString()); // Importante para BigDecimal
        params.put("limit_price", limitPrice.toPlainString()); // Importante para BigDecimal

        return postPrivateApiRequest(params, PlaceOrderResponse.class);
    }

    // Helper para construir a requisição POST com assinatura
    private <T> T postPrivateApiRequest(Map<String, String> requestParams, Class<T> responseType) {
        try {
            // Constrói a string de parâmetros no formato x-www-form-urlencoded
            StringBuilder queryStringBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                if (queryStringBuilder.length() > 0) {
                    queryStringBuilder.append("&");
                }
                queryStringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
            String queryString = queryStringBuilder.toString();

            // Calcula a assinatura HMAC
            String signature = HmacSha512Util.calculateHMAC(queryString, secretKey);

            // Monta o corpo da requisição POST
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            requestParams.forEach(body::add); // Adiciona todos os parâmetros ao corpo da requisição

            return mercadoBitcoinPrivateWebClient.post()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("TAPI-ID", clientId)
                    .header("TAPI-MAC", signature)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(responseType)
                    .block(); // Bloqueia para obter o resultado síncrono
        } catch (Exception e) {
            log.error("Erro ao chamar API privada do Mercado Bitcoin: {}", e.getMessage(), e);
            // Em um ambiente de produção, você pode querer lançar uma exceção específica ou retornar um Optional
            return null;
        }
    }
}
