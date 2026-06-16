package com.scasistemas.msbluedot.controller;

import com.scasistemas.msbluedot.cloudflare.CloudflareImageClient;
import com.scasistemas.msbluedot.config.CloudflareProperties;
import com.scasistemas.msbluedot.dto.HealthResponse;
import com.scasistemas.msbluedot.repository.EmpresaRepository;
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
@RequestMapping("/api/v2/health")
@RequiredArgsConstructor
@Tag(name = "Health v2", description = "Verificação de saúde da aplicação")
public class HealthController {

    private final EmpresaRepository empresaRepository;
    private final CloudflareImageClient cloudflareImageClient;
    private final CloudflareProperties cloudflareProperties;

    @GetMapping
    @Operation(summary = "Verificar status dos serviços dependentes",
            description = "Retorna status do banco de dados e da API Cloudflare. Endpoint público.", security = {})
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

    private String checkBanco() {
        try {
            empresaRepository.count();
            return "UP";
        } catch (Exception e) {
            log.warn("Health check banco falhou: {}", e.getMessage());
            return "DOWN";
        }
    }

    private String checkCloudflare() {
        try {
            boolean ok = cloudflareImageClient.verifyToken(cloudflareProperties.getGetToken());
            return ok ? "UP" : "DOWN";
        } catch (Exception e) {
            log.warn("Health check Cloudflare falhou: {}", e.getMessage());
            return "DOWN";
        }
    }
}
