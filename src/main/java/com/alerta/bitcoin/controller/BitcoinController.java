package com.alerta.bitcoin.controller;

import com.alerta.bitcoin.model.BalanceResponse;
import com.alerta.bitcoin.model.CurrencyBalance;
import com.alerta.bitcoin.service.MercadoBitcoinService;
//import com.alerta.bitcoin.service.TradingBotService;
import com.alerta.bitcoin.service.NtfyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/bitcoin")
public class BitcoinController {

    @Autowired
    private MercadoBitcoinService mercadoBitcoinService;

    @Autowired
    private NtfyService ntfyService;

    @GetMapping("/ticker")
    public String getTicker() {
        String ticker = mercadoBitcoinService.getBitcoinTicker();
        ntfyService.sendNotification("Ticker do Bitcoin obtido: " + ticker.substring(0, Math.min(ticker.length(), 50)) + "...");
        return ticker;
    }

    @GetMapping("/balance") // Novo endpoint para obter e notificar o saldo
    public BalanceResponse getBalance() {
        BalanceResponse balanceResponse = mercadoBitcoinService.getAccountBalance();

        if (balanceResponse != null && balanceResponse.getResponseData() != null && balanceResponse.getResponseData().getBalance() != null) {
            CurrencyBalance btcBalance = balanceResponse.getResponseData().getBalance().getBtc();
            CurrencyBalance brlBalance = balanceResponse.getResponseData().getBalance().getBrl();

            String notificationMessage = String.format(
                    "Saldo MB: BTC %.8f (Disp: %.8f), BRL %.2f (Disp: %.2f)",
                    Double.parseDouble(btcBalance.getTotal()), Double.parseDouble(btcBalance.getAvailable()),
                    Double.parseDouble(brlBalance.getTotal()), Double.parseDouble(brlBalance.getAvailable())
            );
            ntfyService.sendNotification(notificationMessage);
        } else {
            ntfyService.sendNotification("Erro ao obter saldo do Mercado Bitcoin.");
        }
        return balanceResponse;
    }

    @GetMapping("/test-ntfy")
    public String testNtfy() {
        ntfyService.sendNotification("Olá do meu Bot Bitcoin via NTFY!");
        return "Notificação de teste enviada!";
    }
}