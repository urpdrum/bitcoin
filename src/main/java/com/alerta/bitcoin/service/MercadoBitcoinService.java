package com.alerta.bitcoin.service;

//import com.alerta.bitcoin.dto.MercadoBitcoinTickerResponse;
import com.alerta.bitcoin.model.BalanceResponse;
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



@Service
@RequiredArgsConstructor
public class MercadoBitcoinService {
    private static final String MERCADO_BITCOIN_TICKER_URL = "https://www.mercadobitcoin.net/api/BTC/ticker/";
    private static final String MERCADO_BITCOIN_TAPI_URL = "https://www.mercadobitcoin.net/tapi/v4/";
    @Value("${mercadobitcoin.api.key}")
    private String apiKey;

    @Value("${mercadobitcoin.api.secret}")
    private String apiSecret; // PIN API

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Para converter JSON

    public String getBitcoinTicker() {
        return restTemplate.getForObject(MERCADO_BITCOIN_TICKER_URL, String.class);
    }

    public BalanceResponse getAccountBalance() {
        String method = "get_account_info";
        long tapiId = MercadoBitcoinApiUtil.getUnixTimestamp(); // Timestamp em segundos

        // Parâmetros da requisição POST
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("tapi_method", method);
        body.add("tapi_nonce", String.valueOf(tapiId)); // A API do MB usa tapi_nonce aqui

        // A string a ser assinada é tapi_method=get_account_info&tapi_nonce=SEUTIMESTAMP
        // Certifique-se de que os parâmetros estão em ordem alfabética para a assinatura
        String queryString = "tapi_method=" + method + "&tapi_nonce=" + tapiId;

        // Gera a assinatura HMAC-SHA512
        String signature = MercadoBitcoinApiUtil.generateSignature(apiSecret, queryString);

        // Cabeçalhos da requisição
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Formato de envio de dados
        headers.add("TAPI-ID", apiKey);
        headers.add("TAPI-MAC", signature);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Realiza a requisição POST
            // Usamos Exchange para ter mais controle sobre a resposta e o corpo
            // O método é POST, a URL da TAPI v3, a requisição que criamos, e esperamos BalanceResponse
            BalanceResponse response = restTemplate.postForObject(MERCADO_BITCOIN_TAPI_URL, request, BalanceResponse.class);
            return response;
        } catch (Exception e) {
            System.err.println("Erro ao obter saldo da API do Mercado Bitcoin: " + e.getMessage());
            e.printStackTrace();
            return null; // Ou lance uma exceção mais específica
        }
    }
}
