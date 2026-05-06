package com.scasistemas.msbluedot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de carregamento do contexto Spring.
 *
 * <p>
 * Valida que toda a configuração do projeto está correta
 * e que o contexto Spring sobe sem erros.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
class MsBluedotApplicationTests {

    @Test
    void contextLoads() {
        // Valida que o contexto Spring inicializa corretamente
    }

}



