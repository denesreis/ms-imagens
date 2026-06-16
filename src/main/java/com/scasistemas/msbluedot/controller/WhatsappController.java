package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.dto.WhatsappEnviarRequest;
import com.scasistemas.msbluedot.dto.WhatsappEnviarResponse;
import com.scasistemas.msbluedot.security.annotation.IsUsuario;
import com.scasistemas.msbluedot.service.WhatsappService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v2/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp v2", description = "Envio de mensagens via WhatsApp (Evolution API)")
public class WhatsappController {

    private final WhatsappService whatsappService;

    @PostMapping(value = "/enviar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsUsuario
    @Operation(
            summary = "Enviar mensagem WhatsApp",
            description = "Envia uma mensagem de texto e/ou documento (PDF ou XML) via WhatsApp. "
                    + "Campo 'dados' = JSON com telefone e mensagem. "
                    + "Campo 'arquivo' = PDF ou XML (opcional).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensagem enviada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (telefone, tipo de arquivo, tamanho)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "502", description = "Evolution API indisponível")
    })
    public ResponseEntity<WhatsappEnviarResponse> enviar(
            @RequestPart("dados") @Valid WhatsappEnviarRequest request,
            @RequestPart(name = "arquivo", required = false) MultipartFile arquivo) throws IOException {

        log.info("Envio WhatsApp → telefone={} arquivo={}",
                request.getTelefone(),
                arquivo != null ? arquivo.getOriginalFilename() : "nenhum");

        WhatsappEnviarResponse response = whatsappService.enviar(request, arquivo);
        return ResponseEntity.ok(response);
    }
}
