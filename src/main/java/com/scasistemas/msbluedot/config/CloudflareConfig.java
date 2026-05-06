package com.scasistemas.msbluedot.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuração do {@link WebClient} para integração com a Cloudflare Images
 * API.
 *
 * <p>
 * Configura:
 * <ul>
 * <li>Timeout de conexão e leitura</li>
 * <li>Pool de conexões Netty</li>
 * <li>Buffer máximo de 16 MB para uploads</li>
 * <li>Filtro de log de requisições (apenas em DEBUG)</li>
 * </ul>
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CloudflareConfig {

    private final CloudflareProperties cloudflareProperties;

    @PostConstruct
    public void logConfig() {
        String accountId = cloudflareProperties.getAccountId();
        log.info("[Cloudflare] Config carregada — accountId='{}' baseUrl='{}'",
                (accountId != null && !accountId.isBlank())
                        ? accountId.substring(0, Math.min(8, accountId.length())) + "..."
                        : "*** VAZIO ***",
                cloudflareProperties.getBaseUrl());
        if (accountId == null || accountId.isBlank()) {
            log.error("[Cloudflare] ATENÇÃO: accountId não configurado! Uploads vão falhar. " +
                    "Configure 'app.cloudflare.account-id' no application-dev.yml ou via var de ambiente CLOUDFLARE_ACCOUNT_ID.");
        }
    }

    /**
     * WebClient configurado para a Cloudflare API.
     *
     * <p>
     * Injetável via {@code @Qualifier("cloudflareWebClient")}.
     * </p>
     */
    @Bean("cloudflareWebClient")
    public WebClient cloudflareWebClient() {
        int connectTimeoutMs = cloudflareProperties.getTimeout().getConnect();
        int readTimeoutMs = cloudflareProperties.getTimeout().getRead();

        ConnectionProvider connectionProvider = ConnectionProvider.builder("cloudflare-pool")
                .maxConnections(50)
                .pendingAcquireMaxCount(100)
                .pendingAcquireTimeout(Duration.ofSeconds(15))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .resolver(DefaultAddressResolverGroup.INSTANCE) // usa DNS do JDK (evita falha AAAA do Netty)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(cloudflareProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> {
                    // 16 MB de buffer para suportar uploads de imagem
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                })
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // Filtros de log (apenas nível DEBUG para não expor tokens em prod)
    // ─────────────────────────────────────────────────────────────

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            if (log.isDebugEnabled()) {
                log.debug("[Cloudflare] → {} {}", request.method(), request.url());
            }
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (log.isDebugEnabled()) {
                log.debug("[Cloudflare] ← HTTP {}", response.statusCode().value());
            }
            return Mono.just(response);
        });
    }
}

