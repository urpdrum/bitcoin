package com.alerta.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class OrderResponse {
    @JsonProperty("response_data")
    private OrderResponseData responseData;
    @JsonProperty("status_code")
    private int statusCode;
    @JsonProperty("server_unix_timestamp")
    private String serverUnixTimestamp;

    public int getStatusCode() {
        return statusCode;
    }

    // Classe aninhada para o response_data
    @Data
    public static class OrderResponseData {
        private Order order;
    }

    // Classe aninhada para os detalhes da ordem
    @Data
    public static class Order {
        private String order_id;
        private String coin_pair;
        private String order_type; // "buy" ou "sell"
        private String status;     // "active", "executed", "cancelled"
        private String has_fills;
        private String quantity;
        private String limit_price;
        private String executed_quantity;
        private String executed_price_avg;
        private String fee;
        private String created_timestamp;
        private String updated_timestamp;
    }
}
