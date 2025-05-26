# Usar uma imagem base oficial do OpenJDK 17 (ou a versão Java que você está usando)
# A tag -slim ou -alpine é menor, mas pode faltar algumas ferramentas. -jdk é mais completa.
FROM openjdk:17-jdk-slim

# Definir o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copiar o arquivo JAR construído para o diretório de trabalho no contêiner
# Adapte o nome do JAR se necessário (ex: se tiver snapshot ou versão específica)
COPY --from=builder /build-app/target/bitcoin-0.0.1-SNAPSHOT.jar app.jar
# Se usar Gradle, o caminho seria algo como:
# COPY build/libs/bitcoin-desktop-alert-0.0.1-SNAPSHOT.jar app.jar

# Expor a porta que a aplicação Spring Boot usa (padrão 8080)
# Isso é mais para documentação e para o Docker mapear portas facilmente,
# mas a plataforma de hospedagem pode ignorar e usar a porta que ela detectar.
EXPOSE 8080

# Comando para rodar a aplicação quando o contêiner iniciar
# Adicionamos -Djava.awt.headless=false aqui também, caso seja relevante para SystemTray
# embora em um ambiente de servidor, SystemTray provavelmente não funcionará.
# Se as notificações de desktop não são o foco no servidor, pode remover.
ENTRYPOINT ["java", "-Djava.awt.headless=false", "-jar", "app.jar"]

# Opcional: Adicionar variáveis de ambiente diretamente no Dockerfile
# No entanto, é melhor configurar isso na plataforma de hospedagem.
# ENV NTFY_TOPIC="seu-topico-aqui"
# ENV BITCOIN_CURRENCY="usd"