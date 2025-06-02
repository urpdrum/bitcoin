package com.alerta.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BalanceResponse {
    @JsonProperty("response_data")
    private ResponseData responseData;
    @JsonProperty("status_code")
    private int statusCode;
    @JsonProperty("server_unix_timestamp")
    private String serverUnixTimestamp;
}

