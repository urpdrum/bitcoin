package com.alerta.bitcoin;

//import com.alerta.bitcoin.service.NtfyNotificationService;
import com.alerta.bitcoin.model.TickerResponse;
import com.alerta.bitcoin.service.MercadoBitcoinService;
import com.alerta.bitcoin.service.NtfyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication // Habilita o agendamento de tarefas
public class BitcoinApplication{

	public static void main(String[] args) {
		SpringApplication.run(BitcoinApplication.class, args);
	}
//	@Bean
//	public CommandLineRunner run(MercadoBitcoinService mercadoBitcoinService, NtfyService ntfyService) { // Injetar NtfyService
//		return args -> {
//			System.out.println("----------------------------------------------");
//			System.out.println("Teste de Comunicação com Mercado Bitcoin API e NTFY");
//			System.out.println("----------------------------------------------");
//
//			TickerResponse btcTicker = mercadoBitcoinService.getCurrentTicker("BTC");
//			if (btcTicker != null) {
//				String message = String.format("BTC: Último=%.2f, Compra=%.2f, Venda=%.2f",
//						//btcTicker.ticker().last(), btcTicker.ticker().buy(), btcTicker.ticker().sell());
//						btcTicker.ticker().last(), btcTicker.ticker().buy(), btcTicker.ticker().sell());
//				System.out.println(message);
//				ntfyService.sendMessage("Mercado Bitcoin Robô", message, "robot,chart_with_upwards_trend"); // Enviar notificação
//			} else {
//				System.out.println("Não foi possível obter o ticker do BTC.");
//				ntfyService.sendMessage("Mercado Bitcoin Robô", "ERRO: Não foi possível obter o ticker do BTC.", "warning,x");
//			}
//			System.out.println("----------------------------------------------");
//			System.out.println("Teste concluído.");
//		};
//	}
}
