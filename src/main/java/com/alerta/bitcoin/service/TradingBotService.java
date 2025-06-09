package com.alerta.bitcoin.service;

import com.alerta.bitcoin.domain.Wallet;
import com.alerta.bitcoin.model.TickerResponse;
import com.alerta.bitcoin.strategy.SimplePriceThresholdStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TradingBotService {
    private static final Logger log = LoggerFactory.getLogger(TradingBotService.class);

    private final MercadoBitcoinService mercadoBitcoinService;
    private final NtfyService ntfyService;
    private final SimplePriceThresholdStrategy strategy;
    private final Wallet wallet; // Nossa carteira de simulação

    @Value("${bot.simulation-mode}")
    private boolean simulationMode; // Flag para modo de simulação

    // Injeção de dependências
    public TradingBotService(MercadoBitcoinService mercadoBitcoinService,
                             NtfyService ntfyService,
                             SimplePriceThresholdStrategy strategy,
                             @Value("${bot.initial-brl-balance}") BigDecimal initialBrlBalance) {
        this.mercadoBitcoinService = mercadoBitcoinService;
        this.ntfyService = ntfyService;
        this.strategy = strategy;
        this.wallet = new Wallet(initialBrlBalance); // Inicializa a carteira
        log.info("Robô de negociação inicializado. Modo de simulação: {}", simulationMode);
        log.info("Saldo inicial da carteira: {}", wallet);
    }

    // Método agendado para rodar a cada X milissegundos
    // fixedRate = 60000ms (1 minuto)
    @Scheduled(fixedRate = 60000)
    public void runTradingCycle() {
        log.info("Iniciando ciclo de negociação. Saldo atual: {}", wallet);

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
                // Preço de compra será o "buy" price, pois é o preço de mercado para comprar imediatamente
                BigDecimal buyPrice = btcTicker.ticker().buy();
                BigDecimal amountToBuy = strategy.getTradeAmountBrl();
                BigDecimal btcAmount = amountToBuy.divide(buyPrice, 8, RoundingMode.HALF_UP); // 8 casas para BTC

                log.info("DECISÃO: Comprar BTC. Quantidade BRL: {}. Preço de compra: {}", amountToBuy, buyPrice);

                if (!simulationMode) {
                    // TODO: AQUI É ONDE VOCÊ CHAMARIA A API PRIVADA DO MERCADO BITCOIN PARA REALIZAR A COMPRA
                    // MercadoBitcoinPrivateApiClient.placeOrder("buy", btcAmount, buyPrice);
                    log.warn("Modo REAL: Chamada à API de compra desabilitada por segurança.");
                    // Por enquanto, apenas registramos
                }

                // Atualiza a carteira simulada
                wallet.setBrlBalance(wallet.getBrlBalance().subtract(amountToBuy));
                wallet.setBitcoinBalance(wallet.getBitcoinBalance().add(btcAmount));
                wallet.setLastBuyPrice(buyPrice); // Registra o preço da compra
                wallet.setHoldingBitcoin(true);

                String msg = String.format("COMPRA SIMULADA de %.8f BTC por R$%.2f cada. Saldo BRL: R$%.2f, BTC: %.8f. Preço da compra: R$%.2f",
                        btcAmount, buyPrice, wallet.getBrlBalance(), wallet.getBitcoinBalance(), wallet.getLastBuyPrice());
                log.info(msg);
                ntfyService.sendMessage("MB Bot - COMPRA", msg, "moneybag,arrow_down");

            } else {
                log.warn("Não há saldo em BRL suficiente para comprar. Saldo BRL: {}", wallet.getBrlBalance());
                ntfyService.sendMessage("MB Bot - ALERTA", "Saldo BRL insuficiente para comprar BTC.", "warning,no_entry");
            }
        }

        // Lógica de Venda
        else if (strategy.shouldSell(currentPrice, wallet)) {
            if (wallet.getBitcoinBalance().compareTo(BigDecimal.ZERO) > 0) {
                // Preço de venda será o "sell" price, pois é o preço de mercado para vender imediatamente
                BigDecimal sellPrice = btcTicker.ticker().sell();
                BigDecimal amountToSell = wallet.getBitcoinBalance(); // Vende todo o BTC que temos (simplificado)

                log.info("DECISÃO: Vender BTC. Quantidade BTC: {}. Preço de venda: {}", amountToSell, sellPrice);

                if (!simulationMode) {
                    // TODO: AQUI É ONDE VOCÊ CHAMARIA A API PRIVADA DO MERCADO BITCOIN PARA REALIZAR A VENDA
                    // MercadoBitcoinPrivateApiClient.placeOrder("sell", amountToSell, sellPrice);
                    log.warn("Modo REAL: Chamada à API de venda desabilitada por segurança.");
                    // Por enquanto, apenas registramos
                }

                // Atualiza a carteira simulada
                wallet.setBrlBalance(wallet.getBrlBalance().add(amountToSell.multiply(sellPrice)));
                wallet.setBitcoinBalance(BigDecimal.ZERO);
                wallet.setHoldingBitcoin(false);
                wallet.setLastBuyPrice(BigDecimal.ZERO); // Reseta o preço da última compra

                String msg = String.format("VENDA SIMULADA de %.8f BTC por R$%.2f cada. Saldo BRL: R$%.2f, BTC: %.8f. Lucro/Prejuízo: %.2f",
                        amountToSell, sellPrice, wallet.getBrlBalance(), wallet.getBitcoinBalance(),
                        wallet.getBrlBalance().subtract(strategy.getTradeAmountBrl()
                                .add(wallet.getBrlBalance().subtract(amountToSell.multiply(sellPrice))))); // Isso é uma simplificação, cálculo real de lucro é mais complexo
                log.info(msg);
                ntfyService.sendMessage("MB Bot - VENDA", msg, "money_with_wings,chart_with_downwards_trend");

            } else {
                log.warn("Não há Bitcoin para vender.");
            }
        } else {
            log.info("Nenhuma condição de compra ou venda atendida neste ciclo.");
        }

        log.info("Fim do ciclo de negociação. Saldo final: {}", wallet);
    }
}
