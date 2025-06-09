package com.alerta.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TickerResponse( // <-- Mude "class" para "record" aqui!
                              @JsonProperty("ticker") TickerData ticker
) {}