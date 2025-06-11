package com.alerta.bitcoin.service;

import com.alerta.bitcoin.client.MercadoBitcoinPrivateApiClient;
import com.alerta.bitcoin.domain.Wallet;
import com.alerta.bitcoin.model.TickerResponse;
import com.alerta.bitcoin.model.privateapi.AccountInfoResponse;
import com.alerta.bitcoin.model.privateapi.PlaceOrderResponse;
import com.alerta.bitcoin.strategy.SimplePriceThresholdStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class TradingBotService {
    private static final Logger log = LoggerFactory.getLogger(TradingBotService.class);

    private final MercadoBitcoinService mercadoBitcoinService;
    private final MercadoBitcoinPrivateApiClient mercadoBitcoinPrivateApiClient; // Injetar
    private final NtfyService ntfyService;
    private final SimplePriceThresholdStrategy strategy;
    private final Wallet wallet; // Nossa carteira de simulação

    @Value("${bot.simulation-mode}")
    private boolean simulationMode; // Flag para modo de simulação

    // Injeção de dependências
    public TradingBotService(MercadoBitcoinService mercadoBitcoinService,
                             MercadoBitcoinPrivateApiClient mercadoBitcoinPrivateApiClient, // Injetar
                             NtfyService ntfyService,
                             SimplePriceThresholdStrategy strategy,
                             @Value("${bot.initial-brl-balance}") BigDecimal initialBrlBalance) {
        this.mercadoBitcoinService = mercadoBitcoinService;
        this.mercadoBitcoinPrivateApiClient = mercadoBitcoinPrivateApiClient; // Atribuir
        this.ntfyService = ntfyService;
        this.strategy = strategy;
        this.wallet = new Wallet(initialBrlBalance); // Inicializa a carteira
        log.info("Robô de negociação inicializado. Modo de simulação: {}", simulationMode ? "ATIVADO" : "DESATIVADO (operando real!)");
        log.info("Saldo inicial da carteira SIMULADA: {}", wallet);
    }

    // Método agendado para rodar a cada X milissegundos
    @Scheduled(fixedRate = 60000) // 1 minuto
    public void runTradingCycle() {
        log.info("Iniciando ciclo de negociação.");

        // Se não estiver em modo de simulação, atualize o saldo real da carteira
        if (!simulationMode) {
            log.info("Modo REAL: Buscando saldo da carteira do Mercado Bitcoin...");
            AccountInfoResponse accountInfo = mercadoBitcoinPrivateApiClient.getAccountInfo();
            if (accountInfo != null && accountInfo.statusCode() == 100 && accountInfo.responseData() != null) {
                BigDecimal brlAvailable = Optional.ofNullable(accountInfo.responseData().balance().get("BRL"))
                        .map(b -> b.available())
                        .orElse(BigDecimal.ZERO);
                BigDecimal btcAvailable = Optional.ofNullable(accountInfo.responseData().balance().get("BTC"))
                        .map(b -> b.available())
                        .orElse(BigDecimal.ZERO);
                wallet.setBrlBalance(brlAvailable);
                wallet.setBitcoinBalance(btcAvailable);
                wallet.setHoldingBitcoin(btcAvailable.compareTo(BigDecimal.ZERO) > 0);
                log.info("Saldo REAL atualizado: {}", wallet);
            } else {
                String errorMsg = (accountInfo != null) ? "Status: " + accountInfo.statusCode() : "Resposta nula";
                log.error("Falha ao obter saldo real da carteira: {}", errorMsg);
                ntfyService.sendMessage("MB Bot - ERRO", "Falha ao obter saldo real: " + errorMsg, "warning,x");
                return; // Não prossegue com o ciclo se não conseguir o saldo real
            }
        }

        TickerResponse btcTicker = mercadoBitcoinService.getCurrentTicker("BTC");
        if (btcTicker == null || btcTicker.ticker() == null) {
            log.error("Não foi possível obter o ticker do BTC. Pulando este ciclo.");
            ntfyService.sendMessage("MB Bot - ERRO", "Não foi possível obter o ticker do BTC.", "warning,x");
            return;
        }

        BigDecimal currentPrice = btcTicker.ticker().last();
        log.info("Preço atual do BTC: {}", currentPrice);

        // Atualiza o preço "histórico" na estratégia para referência futura de compra
        strategy.setLastKnownPrice(currentPrice);

        // Lógica de Compra
        if (strategy.shouldBuy(currentPrice, wallet)) {
            if (wallet.getBrlBalance().compareTo(strategy.getTradeAmountBrl()) >= 0) {
                BigDecimal buyPrice = btcTicker.ticker().buy(); // Preço de mercado para comprar
                BigDecimal amountToBuy = strategy.getTradeAmountBrl();
                BigDecimal btcAmount = amountToBuy.divide(buyPrice, 8, RoundingMode.HALF_UP);

                log.info("DECISÃO: Comprar BTC. Quantidade BRL: R${}. Preço de compra: R${}. Quantidade BTC: {}",
                        amountToBuy, buyPrice, btcAmount);

                if (!simulationMode) {
                    PlaceOrderResponse orderResponse = mercadoBitcoinPrivateApiClient.placeBuyOrder("BRLBTC", btcAmount, buyPrice);
                    if (orderResponse != null && orderResponse.statusCode() == 100) {
                        log.info("COMPRA REALIZADA! Ordem ID: {}", orderResponse.responseData().order().orderId());
                        // Atualiza saldo REAL após a operação (ou numa próxima requisição getAccountInfo)
                        // Para este exemplo, vamos simplificar a atualização da carteira simulada
                        wallet.setBrlBalance(wallet.getBrlBalance().subtract(amountToBuy));
                        wallet.setBitcoinBalance(wallet.getBitcoinBalance().add(btcAmount));
                        wallet.setLastBuyPrice(buyPrice);
                        wallet.setHoldingBitcoin(true);
                        String msg = String.format("COMPRA REAL de %.8f BTC por R$%.2f cada. Saldo BRL: R$%.2f, BTC: %.8f. ID Ordem: %d",
                                btcAmount, buyPrice, wallet.getBrlBalance(), wallet.getBitcoinBalance(), orderResponse.responseData().order().orderId());
                        ntfyService.sendMessage("MB Bot - COMPRA REAL", msg, "moneybag,arrow_down");
                    } else {
                        String errorMsg = (orderResponse != null) ? "Status: " + orderResponse.statusCode() + ", Erro: " + orderResponse.errorMessage() : "Resposta nula";
                        log.error("FALHA na COMPRA REAL: {}", errorMsg);
                        ntfyService.sendMessage("MB Bot - ERRO na COMPRA", "Falha na compra: " + errorMsg, "warning,x");
                    }
                } else {
                    // Atualiza a carteira SIMULADA
                    wallet.setBrlBalance(wallet.getBrlBalance().subtract(amountToBuy));
                    wallet.setBitcoinBalance(wallet.getBitcoinBalance().add(btcAmount));
                    wallet.setLastBuyPrice(buyPrice);
                    wallet.setHoldingBitcoin(true);
                    String msg = String.format("COMPRA SIMULADA de %.8f BTC por R$%.2f cada. Saldo BRL: R$%.2f, BTC: %.8f. Preço da compra: R$%.2f",
                            btcAmount, buyPrice, wallet.getBrlBalance(), wallet.getBitcoinBalance(), wallet.getLastBuyPrice());
                    log.info(msg);
                    ntfyService.sendMessage("MB Bot - COMPRA SIMULADA", msg, "moneybag,arrow_down");
                }

            } else {
                log.warn("Não há saldo em BRL suficiente para comprar. Saldo BRL: {}", wallet.getBrlBalance());
                ntfyService.sendMessage("MB Bot - ALERTA", "Saldo BRL insuficiente para comprar BTC.", "warning,no_entry");
            }
        }

        // Lógica de Venda
        else if (strategy.shouldSell(currentPrice, wallet)) {
            if (wallet.getBitcoinBalance().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal sellPrice = btcTicker.ticker().sell(); // Preço de mercado para vender
                BigDecimal amountToSell = wallet.getBitcoinBalance(); // Vende todo o BTC (simplificado)

                log.info("DECISÃO: Vender BTC. Quantidade BTC: {}. Preço de venda: R${}", amountToSell, sellPrice);

                if (!simulationMode) {
                    PlaceOrderResponse orderResponse = mercadoBitcoinPrivateApiClient.placeSellOrder("BRLBTC", amountToSell, sellPrice);
                    if (orderResponse != null && orderResponse.statusCode() == 2) {
                        log.info("VENDA REALIZADA! Ordem ID: {}", orderResponse.responseData().order().orderId());
                        // Atualiza saldo REAL após a operação
                        wallet.setBrlBalance(wallet.getBrlBalance().add(amountToSell.multiply(sellPrice)));
                        wallet.setBitcoinBalance(BigDecimal.ZERO);
                        wallet.setHoldingBitcoin(false);
                        wallet.setLastBuyPrice(BigDecimal.ZERO);
                        String msg = String.format("VENDA REAL de %.8f BTC por R$%.2f cada. Saldo BRL: R$%.2f, BTC: %.8f. ID Ordem: %d",
                                amountToSell, sellPrice, wallet.getBrlBalance(), wallet.getBitcoinBalance(), orderResponse.responseData().order().orderId());
                        ntfyService.sendMessage("MB Bot - VENDA REAL", msg, "money_with_wings,chart_with_downwards_trend");
                    } else {
                        String errorMsg = (orderResponse != null) ? "Status: " + orderResponse.statusCode() + ", Erro: " + orderResponse.errorMessage() : "Resposta nula";
                        log.error("FALHA na VENDA REAL: {}", errorMsg);
                        ntfyService.sendMessage("MB Bot - ERRO na VENDA", "Falha na venda: " + errorMsg, "warning,x");
                    }
                } else {
                    // Atualiza a carteira SIMULADA
                    wallet.setBrlBalance(wallet.getBrlBalance().add(amountToSell.multiply(sellPrice)));
                    wallet.setBitcoinBalance(BigDecimal.ZERO);
                    wallet.setHoldingBitcoin(false);
                    wallet.setLastBuyPrice(BigDecimal.ZERO);
                    String msg = String.format("VENDA SIMULADA de %.8f BTC por R$%.2f cada. Saldo BRL: R$%.2f, BTC: %.8f. Preço da venda: R$%.2f",
                            amountToSell, sellPrice, wallet.getBrlBalance(), wallet.getBitcoinBalance(), sellPrice);
                    log.info(msg);
                    ntfyService.sendMessage("MB Bot - VENDA SIMULADA", msg, "money_with_wings,chart_with_downwards_trend");
                }

            } else {
                log.warn("Não há Bitcoin para vender.");
            }
        } else {
            log.info("Nenhuma condição de compra ou venda atendida neste ciclo.");
        }

        log.info("Fim do ciclo de negociação. Saldo FINAL: {}", wallet);
    }
}
