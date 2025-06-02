package com.alerta.bitcoin.service;

//import com.alerta.bitcoin.dto.MercadoBitcoinTickerResponse;
import com.alerta.bitcoin.model.BalanceResponse;
import com.alerta.bitcoin.model.OrderResponse;
import com.alerta.bitcoin.util.MercadoBitcoinApiUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;


@Service
public class MercadoBitcoinService {
    private static final String MERCADO_BITCOIN_TICKER_URL = "https://www.mercadobitcoin.net/api/BTC/ticker/";
    private static final String MERCADO_BITCOIN_TAPI_URL = "https://www.mercadobitcoin.net/tapi/v3/";

    @Value("${mercadobitcoin.api.key}")
    private String apiKey;

    @Value("${mercadobitcoin.api.secret}")
    private String apiSecret; // PIN API

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getBitcoinTicker() {
        return restTemplate.getForObject(MERCADO_BITCOIN_TICKER_URL, String.class);
    }

    public BalanceResponse getAccountBalance() {
        String method = "get_account_info";
        long tapiId = MercadoBitcoinApiUtil.getUnixTimestamp();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("tapi_method", method);
        body.add("tapi_nonce", String.valueOf(tapiId));

        String queryString = "tapi_method=" + method + "&tapi_nonce=" + tapiId;
        String signature = MercadoBitcoinApiUtil.generateSignature(apiSecret, queryString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("TAPI-ID", apiKey);
        headers.add("TAPI-MAC", signature);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(MERCADO_BITCOIN_TAPI_URL, request, BalanceResponse.class);
        } catch (Exception e) {
            System.err.println("Erro ao obter saldo da API do Mercado Bitcoin: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Coloca uma ordem de compra limitada (Limit Order) de BTC.
     * @param quantity Quantidade de BTC a ser comprada (ex: "0.001"). Use BigDecimal para precisão.
     * @param limitPrice Preço máximo pelo qual você está disposto a pagar por BTC (ex: "230000.00"). Use BigDecimal para precisão.
     * @return Objeto OrderResponse contendo os detalhes da ordem.
     */
    public OrderResponse placeBuyOrder(BigDecimal quantity, BigDecimal limitPrice) {
        String method = "place_buy_order";
        long tapiId = MercadoBitcoinApiUtil.getUnixTimestamp();
        String coinPair = "BRLBTC"; // Par de moeda: BRL para BTC

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("tapi_method", method);
        body.add("tapi_nonce", String.valueOf(tapiId));
        body.add("coin_pair", coinPair);
        body.add("quantity", quantity.toPlainString()); // Importante usar toPlainString para precisão
        body.add("limit_price", limitPrice.toPlainString());

        // A string para assinatura deve ter os parâmetros em ordem alfabética!
        String queryString = "coin_pair=" + coinPair +
                "&limit_price=" + limitPrice.toPlainString() +
                "&quantity=" + quantity.toPlainString() +
                "&tapi_method=" + method +
                "&tapi_nonce=" + tapiId;

        String signature = MercadoBitcoinApiUtil.generateSignature(apiSecret, queryString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("TAPI-ID", apiKey);
        headers.add("TAPI-MAC", signature);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            OrderResponse response = restTemplate.postForObject(MERCADO_BITCOIN_TAPI_URL, request, OrderResponse.class);
            if (response != null && response.getStatusCode() == 100) { // 100 geralmente significa sucesso
                System.out.println("Ordem de Compra enviada com sucesso! Order ID: " + response.getResponseData().getOrder().getOrder_id());
            } else {
                System.err.println("Erro ao enviar ordem de compra. Status: " + (response != null ? response.getStatusCode() : "N/A"));
            }
            return response;
        } catch (Exception e) {
            System.err.println("Exceção ao enviar ordem de compra: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Coloca uma ordem de venda limitada (Limit Order) de BTC.
     * @param quantity Quantidade de BTC a ser vendida (ex: "0.001"). Use BigDecimal para precisão.
     * @param limitPrice Preço mínimo pelo qual você está disposto a vender BTC (ex: "235000.00"). Use BigDecimal para precisão.
     * @return Objeto OrderResponse contendo os detalhes da ordem.
     */
    public OrderResponse placeSellOrder(BigDecimal quantity, BigDecimal limitPrice) {
        String method = "place_sell_order";
        long tapiId = MercadoBitcoinApiUtil.getUnixTimestamp();
        String coinPair = "BRLBTC"; // Par de moeda: BRL para BTC

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("tapi_method", method);
        body.add("tapi_nonce", String.valueOf(tapiId));
        body.add("coin_pair", coinPair);
        body.add("quantity", quantity.toPlainString());
        body.add("limit_price", limitPrice.toPlainString());

        // A string para assinatura deve ter os parâmetros em ordem alfabética!
        String queryString = "coin_pair=" + coinPair +
                "&limit_price=" + limitPrice.toPlainString() +
                "&quantity=" + quantity.toPlainString() +
                "&tapi_method=" + method +
                "&tapi_nonce=" + tapiId;

        String signature = MercadoBitcoinApiUtil.generateSignature(apiSecret, queryString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("TAPI-ID", apiKey);
        headers.add("TAPI-MAC", signature);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            OrderResponse response = restTemplate.postForObject(MERCADO_BITCOIN_TAPI_URL, request, OrderResponse.class);
            if (response != null && response.getStatusCode() == 100) {
                System.out.println("Ordem de Venda enviada com sucesso! Order ID: " + response.getResponseData().getOrder().getOrder_id());
            } else {
                System.err.println("Erro ao enviar ordem de venda. Status: " + (response != null ? response.getStatusCode() : "N/A"));
            }
            return response;
        } catch (Exception e) {
            System.err.println("Exceção ao enviar ordem de venda: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
