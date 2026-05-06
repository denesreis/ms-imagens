package com.scasistemas.msbluedot;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Configuração de teste para habilitar auditoria JPA nos {@link DataJpaTest}.
 *
 * <p>
 * A anotação {@code @EnableJpaAuditing} na classe principal é ignorada nos
 * testes
 * de slice {@code @DataJpaTest}. Importe esta classe com {@code @Import} quando
 * necessário.
 * </p>
 */
@TestConfiguration
@EnableJpaAuditing
public class JpaAuditingTestConfig {
}

