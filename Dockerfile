# ==============================================================================
# Stage 1: Build
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Baixar dependências em camada separada (cache de build mais eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copiar código-fonte e fazer build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ==============================================================================
# Stage 2: Runtime (Tomcat 10 + JRE 21)
# ==============================================================================
FROM tomcat:10.1-jre21-temurin-alpine AS runtime

# Remover aplicações padrão do Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar o WAR para o Tomcat
COPY --from=build /app/target/ms-bluedot.war /usr/local/tomcat/webapps/ROOT.war

# Criar diretório de logs
RUN mkdir -p /var/log/ms-bluedot

# Expor porta HTTP
EXPOSE 8080

# Variáveis de ambiente (valores padrão sobrescritos em produção)
ENV SPRING_PROFILE=prod
ENV SERVER_PORT=8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/health || exit 1

CMD ["catalina.sh", "run"]
