//package com.alerta.bitcoin.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class TradingBotService {
//    private final MercadoBitcoinService mercadoBitcoinService;
//    private final NtfyNotificationService notificationService;
//
//    @Scheduled(fixedRate = 300000) // Executa a cada 5 minutos
//    public void checkMarketAndTrade() {
//        try {
//            Double currentPrice = mercadoService.getCurrentPrice("BTC");
//            notificationService.sendNotification(
//                    "Preço atual do BTC: R$ " + currentPrice,
//                    "Atualização de Mercado"
//            );
//
//            // Lógica de compra/venda virá aqui depois
//        } catch (Exception e) {
//            notificationService.sendNotification(
//                    "Falha na análise: " + e.getMessage(),
//                    "ERRO no Bot"
//            );
//        }
//    }
//}
