package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.TestSecurityHelper;
import com.scasistemas.msbluedot.application.dto.produto.ProdutoRequest;
import com.scasistemas.msbluedot.application.dto.produto.ProdutoResponse;
import com.scasistemas.msbluedot.application.mappers.ProdutoMapper;
import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Produto;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.exceptions.UnauthorizedException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IProdutoRepository;
import org.junit.jupiter.api.AfterEach;
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
@DisplayName("UpdateProdutoUseCase - Testes unitários")
class UpdateProdutoUseCaseTest {

    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private UpdateProdutoUseCase updateProdutoUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("ADMIN deve atualizar qualquer produto incluindo idEmpresa")
    void adminDeveAtualizarQualquerProduto() {
        TestSecurityHelper.mockAdminContext();
        Produto produto = Produto.builder().id("prod-001").idEmpresa(1L).codigoEan("123").build();
        ProdutoRequest request = new ProdutoRequest();
        request.setDescricao("Nova Desc");
        request.setIdEmpresa(2L);
        ProdutoResponse response = new ProdutoResponse();
        response.setId("prod-001");

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);
        when(produtoMapper.toResponse(produto)).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        ProdutoResponse result = updateProdutoUseCase.execute("prod-001", request);

        assertThat(result.getId()).isEqualTo("prod-001");
        assertThat(produto.getIdEmpresa()).isEqualTo(2L);
        verify(auditLogRepository).save(argThat(log -> "ATUALIZAR_PRODUTO".equals(log.getAcao())));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO tenta atualizar produto de outra empresa")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Produto produto = Produto.builder().id("prod-001").idEmpresa(99L).build();

        when(produtoRepository.findById("prod-001")).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> updateProdutoUseCase.execute("prod-001", new ProdutoRequest()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para produto inexistente")
    void deveLancarExcecaoParaProdutoInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(produtoRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateProdutoUseCase.execute("nao-existe", new ProdutoRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

