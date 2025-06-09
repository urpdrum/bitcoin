package com.alerta.bitcoin.domain;

import java.math.BigDecimal;

public class Wallet {
    private BigDecimal bitcoinBalance;
    private BigDecimal brlBalance; // Moeda Fiat
    private BigDecimal lastBuyPrice; // Preço da última compra de BTC
    private boolean holdingBitcoin; // Se estamos atualmente segurando Bitcoin

    public Wallet(BigDecimal initialBrlBalance) {
        this.bitcoinBalance = BigDecimal.ZERO;
        this.brlBalance = initialBrlBalance;
        this.lastBuyPrice = BigDecimal.ZERO;
        this.holdingBitcoin = false;
    }

    public BigDecimal getBitcoinBalance() {
        return bitcoinBalance;
    }

    public void setBitcoinBalance(BigDecimal bitcoinBalance) {
        this.bitcoinBalance = bitcoinBalance;
    }

    public BigDecimal getBrlBalance() {
        return brlBalance;
    }

    public void setBrlBalance(BigDecimal brlBalance) {
        this.brlBalance = brlBalance;
    }

    public BigDecimal getLastBuyPrice() {
        return lastBuyPrice;
    }

    public void setLastBuyPrice(BigDecimal lastBuyPrice) {
        this.lastBuyPrice = lastBuyPrice;
    }

    public boolean isHoldingBitcoin() {
        return holdingBitcoin;
    }

    public void setHoldingBitcoin(boolean holdingBitcoin) {
        this.holdingBitcoin = holdingBitcoin;
    }

    @Override
    public String toString() {
        return "Carteira [BTC=" + bitcoinBalance + ", BRL=" + brlBalance +
                ", UltimaCompra=" + lastBuyPrice + ", ComBitcoin=" + holdingBitcoin + "]";
    }
}
