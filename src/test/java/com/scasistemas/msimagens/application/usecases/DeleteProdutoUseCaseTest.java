package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteProdutoUseCase - Testes unitários")
class DeleteProdutoUseCaseTest {

    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;

    @InjectMocks
    private DeleteProdutoUseCase deleteProdutoUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("ADMIN deve deletar qualquer produto com sucesso")
    void adminDeveDeletarQualquerProduto() {
        TestSecurityHelper.mockAdminContext();
        Produto produto = Produto.builder().id("prod-001").idEmpresa(1L).codigoEan("123").build();

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        deleteProdutoUseCase.execute("prod-001");

        verify(produtoRepository).deleteById("prod-001");
        verify(auditLogRepository).save(argThat(log -> "DELETAR_PRODUTO".equals(log.getAcao())));
    }

    @Test
    @DisplayName("USUARIO deve deletar produto da sua empresa")
    void usuarioDeveDeletarProdutoDaSuaEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Produto produto = Produto.builder().id("prod-001").idEmpresa(1L).codigoEan("123").build();

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));
        when(auditLogRepository.save(any())).thenReturn(null);

        deleteProdutoUseCase.execute("prod-001");

        verify(produtoRepository).deleteById("prod-001");
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO tenta deletar produto de outra empresa")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Produto produto = Produto.builder().id("prod-001").idEmpresa(99L).build();

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> deleteProdutoUseCase.execute("prod-001"))
                .isInstanceOf(UnauthorizedException.class);

        verify(produtoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para produto inexistente")
    void deveLancarExcecaoParaProdutoInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(produtoRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteProdutoUseCase.execute("nao-existe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
