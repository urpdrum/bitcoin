package com.alerta.bitcoin.model.privateapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public record AccountInfoResponse(@JsonProperty("response_data") AccountInfoData responseData,
                                  @JsonProperty("status_code") long statusCode
) {
    // Definimos AccountInfoData como um record aninhado DENTRO de AccountInfoResponse
    // Como é um record, ele é implicitamente static. 'public' é necessário para acessá-lo de fora.
    public record AccountInfoData( // <-- AGORA PODE SER 'public record' AQUI, PORQUE ESTÁ ANINHADO
                                   @JsonProperty("balance") Map<String, Balance> balance,
                                   @JsonProperty("open_orders") Map<String, Object> openOrders
    ) {
    }

    // Definimos Balance como um record aninhado DENTRO de AccountInfoResponse (ou AccountInfoData, mas aqui é mais flexível)
    // Para simplificar, vamos aninhar dentro de AccountInfoResponse também.
    public record Balance( // <-- AGORA PODE SER 'public record' AQUI, PORQUE ESTÁ ANINHADO
                           @JsonProperty("available") BigDecimal available,
                           @JsonProperty("total") BigDecimal total
    ) {
    }
}