package com.alerta.bitcoin.model.privateapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PlaceOrderResponse (@JsonProperty("response_data") OrderData responseData,
                                  @JsonProperty("status_code") long statusCode,
                                  @JsonProperty("error_message") String errorMessage
) {
    // Definimos OrderData como um record aninhado DENTRO de PlaceOrderResponse
    public record OrderData( // <-- AGORA PODE SER 'public record' AQUI
                             @JsonProperty("order") OrderInfo order
    ) {}

    // Definimos OrderInfo como um record aninhado DENTRO de PlaceOrderResponse
    public record OrderInfo( // <-- AGORA PODE SER 'public record' AQUI
                             @JsonProperty("order_id") Long orderId,
                             @JsonProperty("coin_pair") String coinPair,
                             @JsonProperty("order_type") Long orderType,
                             @JsonProperty("status") Long status,
                             @JsonProperty("has_fills") Boolean hasFills,
                             @JsonProperty("quantity") BigDecimal quantity,
                             @JsonProperty("limit_price") BigDecimal limitPrice,
                             @JsonProperty("executed_quantity") BigDecimal executedQuantity,
                             @JsonProperty("executed_price_avg") BigDecimal executedPriceAvg,
                             @JsonProperty("fee") BigDecimal fee,
                             @JsonProperty("created_timestamp") String createdTimestamp,
                             @JsonProperty("updated_timestamp") String updatedTimestamp
    ) {}
}