package com.alerta.bitcoin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TickerData (
    @JsonProperty("high")
    BigDecimal high,    // Maior preço das últimas 24h
    @JsonProperty("low")
    BigDecimal low,      // Menor preço das últimas 24h
    @JsonProperty("vol")
    BigDecimal vol,     // Volume negociado nas últimas 24h
    @JsonProperty("last")
    BigDecimal last,     // Último preço
    @JsonProperty("buy")
    BigDecimal buy,     // Menor preço de oferta de compra
    @JsonProperty("sell")
    BigDecimal sell,     // Maior preço de oferta de venda
    @JsonProperty("date")
    Long date         // Timestamp da consulta
){}
