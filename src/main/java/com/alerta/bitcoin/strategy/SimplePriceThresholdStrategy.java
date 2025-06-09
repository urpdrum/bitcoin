package com.alerta.bitcoin.strategy;

import com.alerta.bitcoin.domain.Wallet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Component
public class SimplePriceThresholdStrategy {
    @Value("${bot.strategy.buy-threshold-percent}")
    private BigDecimal buyThresholdPercent; // Ex: 0.02 para 2% de queda
    @Value("${bot.strategy.sell-threshold-percent}")
    private BigDecimal sellThresholdPercent; // Ex: 0.03 para 3% de alta
    @Value("${bot.trade.amount-brl}")
    private BigDecimal tradeAmountBrl; // Quanto BRL usar por operação

    // Este será o "histórico" mais recente que temos.
    // Em uma estratégia real, você usaria médias móveis, RSI, etc.
    private BigDecimal lastKnownPrice = BigDecimal.ZERO;

    public void setLastKnownPrice(BigDecimal price) {
        this.lastKnownPrice = price;
    }

    public boolean shouldBuy(BigDecimal currentPrice, Wallet wallet) {
        if (wallet.isHoldingBitcoin()) {
            return false; // Já temos Bitcoin, não vamos comprar mais
        }
        if (lastKnownPrice.compareTo(BigDecimal.ZERO) == 0) {
            // Primeira execução ou preço inicial não definido. Não comprar ainda.
            return false;
        }

        // Calcula a porcentagem de queda em relação ao lastKnownPrice
        BigDecimal dropPercentage = lastKnownPrice.subtract(currentPrice)
                .divide(lastKnownPrice, 4, RoundingMode.HALF_UP);

        boolean decision = dropPercentage.compareTo(buyThresholdPercent) >= 0;

        if (decision) {
            System.out.println(String.format("Decisão de COMPRA: Preço atual %.2f, Último conhecido %.2f. Queda de %.2f%% (limite %.2f%%)",
                    currentPrice, lastKnownPrice, dropPercentage.multiply(BigDecimal.valueOf(100)), buyThresholdPercent.multiply(BigDecimal.valueOf(100))));
        }

        return decision;
    }

    public boolean shouldSell(BigDecimal currentPrice, Wallet wallet) {
        if (!wallet.isHoldingBitcoin()) {
            return false; // Não temos Bitcoin para vender
        }

        // Calcula a porcentagem de alta em relação ao preço de compra
        BigDecimal gainPercentage = currentPrice.subtract(wallet.getLastBuyPrice())
                .divide(wallet.getLastBuyPrice(), 4, RoundingMode.HALF_UP);

        boolean decision = gainPercentage.compareTo(sellThresholdPercent) >= 0;

        if (decision) {
            System.out.println(String.format("Decisão de VENDA: Preço atual %.2f, Último comprado %.2f. Ganho de %.2f%% (limite %.2f%%)",
                    currentPrice, wallet.getLastBuyPrice(), gainPercentage.multiply(BigDecimal.valueOf(100)), sellThresholdPercent.multiply(BigDecimal.valueOf(100))));
        }

        return decision;
    }

    public BigDecimal getTradeAmountBrl() {
        return tradeAmountBrl;
    }
}
