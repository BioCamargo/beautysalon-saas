# ==========================================================
# Etapa 1: Build da aplicação (Maven + JDK 17)
# ==========================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# Copia arquivos do Maven Wrapper e POM para cache de dependências
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Torna o executável do Maven wrapper utilizável e baixa dependências
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copia o código-fonte e compila gerando o JAR sem executar testes no build do container
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ==========================================================
# Etapa 2: Runtime enxuto (JRE 17)
# ==========================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Cria usuário não-root para execução segura
RUN groupadd -r beautysalon && useradd -r -g beautysalon beautysalon

# Cria diretório de uploads
RUN mkdir -p /app/uploads && chown -R beautysalon:beautysalon /app

# Copia o artefato construído na etapa anterior
COPY --from=builder --chown=beautysalon:beautysalon /build/target/*.jar /app/app.jar

USER beautysalon

# Expõe a porta padrão da aplicação
EXPOSE 8080 8081

# Variáveis de ambiente padrão com suporte a overrides (Render define PORT automaticamente)
ENV PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Inicializa a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
