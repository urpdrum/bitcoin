package com.alerta.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Balance
    {
        @JsonProperty("btc")
        private CurrencyBalance btc;
        @JsonProperty("brl")
        private CurrencyBalance brl;
        // Se você operar com outras moedas, adicione aqui (ex: "eth", "usdt")
        // @JsonProperty("eth")
        // private CurrencyBalance eth;
    }

