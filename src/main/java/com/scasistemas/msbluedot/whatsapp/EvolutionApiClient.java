package com.scasistemas.msbluedot.whatsapp;

import com.scasistemas.msbluedot.config.EvolutionApiProperties;
import com.scasistemas.msbluedot.exception.WhatsappException;
import com.scasistemas.msbluedot.whatsapp.dto.EvolutionApiResponse;
import com.scasistemas.msbluedot.whatsapp.dto.EvolutionSendMediaRequest;
import com.scasistemas.msbluedot.whatsapp.dto.EvolutionSendTextRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
public class EvolutionApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final WebClient evolutionWebClient;
    private final EvolutionApiProperties properties;

    public EvolutionApiClient(
            @Qualifier("evolutionWebClient") WebClient evolutionWebClient,
            EvolutionApiProperties properties) {
        this.evolutionWebClient = evolutionWebClient;
        this.properties = properties;
    }

    public EvolutionApiResponse enviarTexto(String telefone, String mensagem) {
        String instance = properties.getInstance();
        String apiKey = properties.getApiKey();

        EvolutionSendTextRequest payload = EvolutionSendTextRequest.builder()
                .number(telefone)
                .text(mensagem)
                .build();

        log.info("[WhatsApp] Enviando texto para {} via instância {}", telefone, instance);

        return chamarApi("/message/sendText/" + instance, apiKey, payload);
    }

    public EvolutionApiResponse enviarComArquivo(String telefone, String mensagem,
            byte[] arquivo, String nomeArquivo, String mimeType) {

        String instance = properties.getInstance();
        String apiKey = properties.getApiKey();
        String base64 = Base64.getEncoder().encodeToString(arquivo);

        EvolutionSendMediaRequest payload = EvolutionSendMediaRequest.builder()
                .number(telefone)
                .mediatype("document")
                .mimetype(mimeType)
                .caption(mensagem)
                .media(base64)
                .fileName(nomeArquivo)
                .build();

        log.info("[WhatsApp] Enviando documento '{}' ({}) para {} via instância {}",
                nomeArquivo, mimeType, telefone, instance);

        return chamarApi("/message/sendMedia/" + instance, apiKey, payload);
    }

    private EvolutionApiResponse chamarApi(String path, String apiKey, Object payload) {
        try {
            EvolutionApiResponse response = evolutionWebClient.post()
                    .uri(path)
                    .header("apikey", apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .map(body -> new WhatsappException(
                                    "Evolution API retornou erro: " + resp.statusCode().value() + " — " + body)))
                    .bodyToMono(EvolutionApiResponse.class)
                    .block(TIMEOUT);

            if (response == null) {
                throw new WhatsappException("Evolution API retornou resposta vazia.");
            }

            log.info("[WhatsApp] Mensagem enviada — id={} status={}", response.getMensagemId(), response.getStatus());
            return response;

        } catch (WhatsappException e) {
            throw e;
        } catch (WebClientRequestException e) {
            throw new WhatsappException("Evolution API indisponível: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new WhatsappException("Falha ao chamar Evolution API: " + e.getMessage(), e);
        }
    }
}
