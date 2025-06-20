package com.alerta.bitcoin;

//import com.alerta.bitcoin.service.NtfyNotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling  // Habilita o agendamento de tarefas
public class BitcoinApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");
		SpringApplication.run(BitcoinApplication.class, args);
		// Teste rápido (remova depois)
//		NtfyNotificationService notificationService = new NtfyNotificationService(new RestTemplate());
//		notificationService.sendNotification("Bot iniciado com sucesso!", "Status do Bot");


	}
}
