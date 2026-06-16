package com.scasistemas.msbluedot.service;

import com.scasistemas.msbluedot.config.EvolutionApiProperties;
import com.scasistemas.msbluedot.dto.WhatsappEnviarRequest;
import com.scasistemas.msbluedot.dto.WhatsappEnviarResponse;
import com.scasistemas.msbluedot.exception.InvalidDataException;
import com.scasistemas.msbluedot.whatsapp.EvolutionApiClient;
import com.scasistemas.msbluedot.whatsapp.dto.EvolutionApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsappService {

    private static final List<String> MIME_TYPES_PERMITIDOS = List.of(
            "application/pdf",
            "application/xml",
            "text/xml"
    );

    private final EvolutionApiClient evolutionApiClient;
    private final EvolutionApiProperties properties;

    public WhatsappEnviarResponse enviar(WhatsappEnviarRequest request, MultipartFile arquivo) throws IOException {
        String telefone = sanitizarTelefone(request.getTelefone());
        String mensagem = request.getMensagem();

        EvolutionApiResponse response;

        if (arquivo != null && !arquivo.isEmpty()) {
            validarArquivo(arquivo);
            byte[] bytes = arquivo.getBytes();
            String nomeArquivo = arquivo.getOriginalFilename() != null
                    ? arquivo.getOriginalFilename()
                    : "arquivo";
            String mimeType = arquivo.getContentType();

            response = evolutionApiClient.enviarComArquivo(telefone, mensagem, bytes, nomeArquivo, mimeType);
        } else {
            response = evolutionApiClient.enviarTexto(telefone, mensagem);
        }

        return WhatsappEnviarResponse.builder()
                .status("ENVIADO")
                .telefone(telefone)
                .mensagemId(response.getMensagemId())
                .timestamp(Instant.now())
                .build();
    }

    private String sanitizarTelefone(String telefone) {
        return telefone.replaceAll("\\D", "");
    }

    private void validarArquivo(MultipartFile arquivo) {
        long maxBytes = (long) properties.getMaxFileSizeMb() * 1024 * 1024;
        if (arquivo.getSize() > maxBytes) {
            throw new InvalidDataException("arquivo",
                    "tamanho excede o máximo permitido de " + properties.getMaxFileSizeMb() + " MB");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !MIME_TYPES_PERMITIDOS.contains(contentType)) {
            throw new InvalidDataException("arquivo",
                    "tipo '" + contentType + "' não suportado. Use: application/pdf, application/xml ou text/xml");
        }
    }
}
