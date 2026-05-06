package com.scasistemas.msbluedot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Springdoc OpenAPI (Swagger UI).
 *
 * <p>
 * Documenta todos os endpoints da API com suporte a autenticação JWT Bearer.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme()));
    }

    private Info buildInfo() {
        return new Info()
                .title("MS Imagens API")
                .description("""
                        Micro-serviço de gerenciamento de imagens integrado com Cloudflare Images API.

                        **Autenticação:** JWT Bearer Token
                        - Access token: 30 minutos
                        - Refresh token: 7 dias

                        **Endpoint público:** GET /imagens/ean/{codigoEan} (sem autenticação)
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("Time de Desenvolvimento")
                        .email("dev@scasistemas.com.br"));
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Informe o JWT access token obtido no endpoint POST /auth/login");
    }

}

