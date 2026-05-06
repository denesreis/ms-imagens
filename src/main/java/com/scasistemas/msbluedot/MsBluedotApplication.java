package com.scasistemas.msbluedot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Classe principal do micro-serviço ms-bluedot.
 *
 * <p>
 * Gerenciamento de imagens de produtos integrado com Cloudflare Images API.
 * </p>
 *
 * <p>
 * Funcionalidades principais:
 * </p>
 * <ul>
 * <li>Upload e gerenciamento de imagens via Cloudflare Images API</li>
 * <li>CRUD de Empresas, Usuários, Produtos e Imagens</li>
 * <li>Autenticação JWT com refresh token e blacklist</li>
 * <li>Consulta pública de imagens por código EAN</li>
 * <li>Compressão de imagens antes do upload (Thumbnailator)</li>
 * <li>Cache de consultas frequentes (Caffeine)</li>
 * </ul>
 *
 * <p>
 * <strong>Arquitetura:</strong> Clean Architecture com Spring Boot 3.4.3 / Java
 * 21 / WAR
 * </p>
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
public class MsBluedotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsBluedotApplication.class, args);
    }

}



