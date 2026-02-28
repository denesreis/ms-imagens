package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.produto.ProdutoRequest;
import com.scasistemas.msimagens.application.dto.produto.ProdutoResponse;
import com.scasistemas.msimagens.application.mappers.ProdutoMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.exceptions.BusinessException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProdutoUseCase - Testes unitários")
class CreateProdutoUseCaseTest {

    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private CreateProdutoUseCase createProdutoUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    private ProdutoRequest buildRequest() {
        ProdutoRequest req = new ProdutoRequest();
        req.setCodigoEan("7891234567890");
        req.setDescricao("Produto Teste");
        req.setIdEmpresa(1L);
        return req;
    }

    @Test
    @DisplayName("Deve criar produto com sucesso como ADMIN")
    void deveCriarProdutoComSucessoComoAdmin() {
        ProdutoRequest request = buildRequest();
        Produto produto = Produto.builder()
                .codigoEan("7891234567890")
                .descricao("Produto Teste")
                .idEmpresa(1L)
                .build();
        Produto salvo = Produto.builder()
                .id("prod-uuid-001")
                .codigoEan("7891234567890")
                .descricao("Produto Teste")
                .idEmpresa(1L)
                .build();
        ProdutoResponse response = new ProdutoResponse();
        response.setId("prod-uuid-001");

        when(produtoRepository.findByCodigoEan("7891234567890"))
                .thenReturn(Optional.empty());
        when(produtoMapper.fromRequest(request)).thenReturn(produto);
        when(produtoRepository.save(any(Produto.class))).thenReturn(salvo);
        when(produtoMapper.toResponse(salvo)).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        ProdutoResponse result = createProdutoUseCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("prod-uuid-001");
        verify(produtoRepository).save(any(Produto.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve criar produto como USUARIO herdando idEmpresa do token")
    void deveCriarProdutoComoUsuarioHerdandoIdEmpresa() {
        TestSecurityHelper.clearContext();
        TestSecurityHelper.mockUsuarioContext();

        ProdutoRequest request = buildRequest();
        request.setIdEmpresa(null); // Usuário não informa idEmpresa

        Produto produto = Produto.builder()
                .codigoEan("7891234567890")
                .descricao("Produto Teste")
                .build();
        Produto salvo = Produto.builder()
                .id("prod-uuid-002")
                .codigoEan("7891234567890")
                .descricao("Produto Teste")
                .idEmpresa(1L)
                .build();
        ProdutoResponse response = new ProdutoResponse();
        response.setId("prod-uuid-002");

        when(produtoRepository.findByCodigoEan("7891234567890"))
                .thenReturn(Optional.empty());
        when(produtoMapper.fromRequest(request)).thenReturn(produto);
        when(produtoRepository.save(any(Produto.class))).thenReturn(salvo);
        when(produtoMapper.toResponse(salvo)).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        ProdutoResponse result = createProdutoUseCase.execute(request);

        assertThat(result).isNotNull();
        verify(produtoRepository).save(argThat(p -> p.getIdEmpresa() != null));
    }

    @Test
    @DisplayName("Deve lançar BusinessException para EAN duplicado na mesma empresa")
    void deveLancarExcecaoParaEanDuplicado() {
        ProdutoRequest request = buildRequest();
        Produto existente = Produto.builder()
                .id("prod-existente")
                .codigoEan("7891234567890")
                .idEmpresa(1L)
                .build();

        when(produtoRepository.findByCodigoEan("7891234567890"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> createProdutoUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("7891234567890");

        verify(produtoRepository, never()).save(any());
    }
}
