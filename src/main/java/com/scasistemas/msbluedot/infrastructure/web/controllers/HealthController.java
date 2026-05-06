package com.scasistemas.msbluedot.infrastructure.web.controllers;

import com.scasistemas.msbluedot.application.dto.health.HealthResponse;
import com.scasistemas.msbluedot.config.CloudflareProperties;
import com.scasistemas.msbluedot.domain.services.ICloudflareImageService;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.EmpresaJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Verificação de saúde da aplicação")
public class HealthController {

    private final EmpresaJpaRepository empresaJpaRepository;
    private final ICloudflareImageService cloudflareImageService;
    private final CloudflareProperties cloudflareProperties;

    @GetMapping
    @Operation(summary = "Verificar status dos serviços dependentes", description = "Retorna status do banco de dados e da API Cloudflare. Endpoint público, não requer autenticação.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status dos serviços (banco e Cloudflare)")
    })
    public ResponseEntity<HealthResponse> health() {
        String bancoStatus = checkBanco();
        String cloudflareStatus = checkCloudflare();

        boolean allUp = "UP".equals(bancoStatus) && "UP".equals(cloudflareStatus);
        HealthResponse response = allUp
                ? HealthResponse.up(bancoStatus, cloudflareStatus)
                : HealthResponse.down(bancoStatus, cloudflareStatus);

        log.info("Health check: banco={} cloudflare={}", bancoStatus, cloudflareStatus);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private String checkBanco() {
        try {
            empresaJpaRepository.count();
            return "UP";
        } catch (Exception e) {
            log.warn("Health check banco falhou: {}", e.getMessage());
            return "DOWN";
        }
    }

    private String checkCloudflare() {
        try {
            boolean ok = cloudflareImageService.verifyToken(cloudflareProperties.getGetToken());
            return ok ? "UP" : "DOWN";
        } catch (Exception e) {
            log.warn("Health check Cloudflare falhou: {}", e.getMessage());
            return "DOWN";
        }
    }
}

