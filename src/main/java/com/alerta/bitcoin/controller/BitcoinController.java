package com.alerta.bitcoin.controller;

import com.alerta.bitcoin.model.BalanceResponse;
import com.alerta.bitcoin.model.CurrencyBalance;
import com.alerta.bitcoin.model.OrderResponse;
import com.alerta.bitcoin.service.MercadoBitcoinService;
//import com.alerta.bitcoin.service.TradingBotService;
import com.alerta.bitcoin.service.NtfyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;


@RestController
@RequestMapping("/api/bitcoin")
public class BitcoinController {

    @Autowired
    private MercadoBitcoinService mercadoBitcoinService;

    @Autowired
    private NtfyService ntfyService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Instanciar ObjectMapper

    // Limites de preço para nossa estratégia simples (ajuste conforme sua preferência)
    private static final BigDecimal BUY_PRICE_THRESHOLD = new BigDecimal("220000.00"); // Comprar se o preço cair para ou abaixo
    private static final BigDecimal SELL_PRICE_THRESHOLD = new BigDecimal("230000.00"); // Vender se o preço subir para ou acima
    private static final BigDecimal TRADE_QUANTITY_BTC = new BigDecimal("0.0005"); // Quantidade de BTC para comprar/vender (min. MB é 0.00001 BTC)
    private static final BigDecimal MIN_BRL_TO_BUY = new BigDecimal("50.00"); // Mínimo de BRL para tentar comprar

    @GetMapping("/ticker")
    public String getTicker() {
        String ticker = mercadoBitcoinService.getBitcoinTicker();
        ntfyService.sendNotification("Ticker do Bitcoin obtido: " + ticker.substring(0, Math.min(ticker.length(), 50)) + "...");
        return ticker;
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        BalanceResponse balanceResponse = mercadoBitcoinService.getAccountBalance();

        if (balanceResponse != null && balanceResponse.getResponseData() != null && balanceResponse.getResponseData().getBalance() != null) {
            CurrencyBalance btcBalance = balanceResponse.getResponseData().getBalance().getBtc();
            CurrencyBalance brlBalance = balanceResponse.getResponseData().getBalance().getBrl();

            String notificationMessage = String.format(
                    "Saldo MB: BTC %.8f (Disp: %.8f), BRL %.2f (Disp: %.2f)",
                    new BigDecimal(btcBalance.getTotal()), new BigDecimal(btcBalance.getAvailable()),
                    new BigDecimal(brlBalance.getTotal()), new BigDecimal(brlBalance.getAvailable())
            );
            ntfyService.sendNotification(notificationMessage);
        } else {
            ntfyService.sendNotification("Erro ao obter saldo do Mercado Bitcoin.");
        }
        return balanceResponse;
    }

    @GetMapping("/trade-decision") // Novo endpoint para simular a decisão de trading
    public String makeTradeDecision() {
        String tickerJson = mercadoBitcoinService.getBitcoinTicker();
        try {
            JsonNode rootNode = objectMapper.readTree(tickerJson);
            BigDecimal currentSellPrice = new BigDecimal(rootNode.path("ticker").path("sell").asText());
            BigDecimal currentBuyPrice = new BigDecimal(rootNode.path("ticker").path("buy").asText());

            String decisionMessage = "";
            OrderResponse orderResponse = null;

            // Obter saldo antes de decidir
            BalanceResponse balance = mercadoBitcoinService.getAccountBalance();
            BigDecimal availableBRL = BigDecimal.ZERO;
            BigDecimal availableBTC = BigDecimal.ZERO;
            if (balance != null && balance.getResponseData() != null && balance.getResponseData().getBalance() != null) {
                availableBRL = new BigDecimal(balance.getResponseData().getBalance().getBrl().getAvailable());
                availableBTC = new BigDecimal(balance.getResponseData().getBalance().getBtc().getAvailable());
            } else {
                decisionMessage = "Não foi possível obter o saldo para tomar decisão de trade.";
                ntfyService.sendNotification(decisionMessage);
                return decisionMessage;
            }

            // Lógica de Compra
            // Se o preço de VENDA atual for <= ao nosso limite de compra E tivermos BRL suficiente
            if (currentSellPrice.compareTo(BUY_PRICE_THRESHOLD) <= 0 && availableBRL.compareTo(MIN_BRL_TO_BUY) >= 0) {
                // Preço de compra será o preço de venda atual para garantir execução rápida (Limit Order)
                BigDecimal purchasePrice = currentSellPrice;
                BigDecimal requiredBRL = TRADE_QUANTITY_BTC.multiply(purchasePrice);

                if (availableBRL.compareTo(requiredBRL) >= 0) {
                    decisionMessage = String.format("DECISÃO: Preço (%.2f) está abaixo ou igual ao limite de compra (%.2f). Tentando comprar %.8f BTC por %.2f BRL.",
                            currentSellPrice, BUY_PRICE_THRESHOLD, TRADE_QUANTITY_BTC, purchasePrice);
                    ntfyService.sendNotification(decisionMessage);

                    orderResponse = mercadoBitcoinService.placeBuyOrder(TRADE_QUANTITY_BTC, purchasePrice);
                    if (orderResponse != null && orderResponse.getStatusCode() == 100) {
                        decisionMessage += "\nORDEM DE COMPRA EXECUTADA! ID: " + orderResponse.getResponseData().getOrder().getOrder_id();
                        ntfyService.sendNotification(decisionMessage);
                    } else {
                        decisionMessage += "\nFALHA AO EXECUTAR ORDEM DE COMPRA!";
                        ntfyService.sendNotification(decisionMessage);
                    }
                } else {
                    decisionMessage = String.format("DECISÃO: Preço (%.2f) está abaixo ou igual ao limite de compra (%.2f), mas BRL disponível (%.2f) é insuficiente para comprar %.8f BTC (%.2f BRL necessários).",
                            currentSellPrice, BUY_PRICE_THRESHOLD, availableBRL, TRADE_QUANTITY_BTC, requiredBRL);
                    ntfyService.sendNotification(decisionMessage);
                }

            }
            // Lógica de Venda
            // Se o preço de COMPRA atual for >= ao nosso limite de venda E tivermos BTC suficiente
            else if (currentBuyPrice.compareTo(SELL_PRICE_THRESHOLD) >= 0 && availableBTC.compareTo(TRADE_QUANTITY_BTC) >= 0) {
                // Preço de venda será o preço de compra atual para garantir execução rápida (Limit Order)
                BigDecimal sellingPrice = currentBuyPrice;
                decisionMessage = String.format("DECISÃO: Preço (%.2f) está acima ou igual ao limite de venda (%.2f). Tentando vender %.8f BTC por %.2f BRL.",
                        currentBuyPrice, SELL_PRICE_THRESHOLD, TRADE_QUANTITY_BTC, sellingPrice);
                ntfyService.sendNotification(decisionMessage);

                orderResponse = mercadoBitcoinService.placeSellOrder(TRADE_QUANTITY_BTC, sellingPrice);
                if (orderResponse != null && orderResponse.getStatusCode() == 100) {
                    decisionMessage += "\nORDEM DE VENDA EXECUTADA! ID: " + orderResponse.getResponseData().getOrder().getOrder_id();
                    ntfyService.sendNotification(decisionMessage);
                } else {
                    decisionMessage += "\nFALHA AO EXECUTAR ORDEM DE VENDA!";
                    ntfyService.sendNotification(decisionMessage);
                }
            }
            else {
                decisionMessage = String.format("DECISÃO: Preço atual de venda: %.2f, preço de compra: %.2f. Nenhum ponto de compra/venda atingido ou saldo insuficiente.", currentSellPrice, currentBuyPrice);
                ntfyService.sendNotification(decisionMessage);
            }

            return decisionMessage + (orderResponse != null ? "\nOrder Response: " + orderResponse.toString() : "");

        } catch (IOException e) {
            String errorMessage = "Erro ao parsear ticker do Bitcoin: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            ntfyService.sendNotification(errorMessage);
            return errorMessage;
        }
    }

    @GetMapping("/test-ntfy")
    public String testNtfy() {
        ntfyService.sendNotification("Olá do meu Bot Bitcoin via NTFY!");
        return "Notificação de teste enviada!";
    }
}