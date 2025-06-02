package com.alerta.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CurrencyBalance {
    @JsonProperty("available")
    private String available; // Usamos String para evitar problemas com casas decimais precisas
    @JsonProperty("total")
    private String total;
}

