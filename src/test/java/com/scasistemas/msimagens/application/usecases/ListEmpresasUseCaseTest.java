package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msimagens.application.mappers.EmpresaMapper;
import com.scasistemas.msimagens.domain.entities.Empresa;
import com.scasistemas.msimagens.domain.repositories.IEmpresaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListEmpresasUseCase - Testes unitários")
class ListEmpresasUseCaseTest {

    @Mock
    private IEmpresaRepository empresaRepository;
    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private ListEmpresasUseCase listEmpresasUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve listar empresas com paginação")
    void deveListarEmpresasComPaginacao() {
        Empresa e1 = Empresa.builder().id(1L).nome("Empresa 1").build();
        Empresa e2 = Empresa.builder().id(2L).nome("Empresa 2").build();
        EmpresaResponse r1 = new EmpresaResponse();
        r1.setId(1L);
        EmpresaResponse r2 = new EmpresaResponse();
        r2.setId(2L);

        when(empresaRepository.findAll()).thenReturn(List.of(e1, e2));
        when(empresaMapper.toResponse(e1)).thenReturn(r1);
        when(empresaMapper.toResponse(e2)).thenReturn(r2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<EmpresaResponse> result = listEmpresasUseCase.execute(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há empresas")
    void deveRetornarPaginaVazia() {
        when(empresaRepository.findAll()).thenReturn(List.of());

        Page<EmpresaResponse> result = listEmpresasUseCase.execute(PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve paginar corretamente quando tem mais itens que o tamanho da página")
    void devePaginarCorretamente() {
        Empresa e1 = Empresa.builder().id(1L).nome("E1").build();
        Empresa e2 = Empresa.builder().id(2L).nome("E2").build();
        Empresa e3 = Empresa.builder().id(3L).nome("E3").build();
        EmpresaResponse r1 = new EmpresaResponse();
        r1.setId(1L);
        EmpresaResponse r2 = new EmpresaResponse();
        r2.setId(2L);
        EmpresaResponse r3 = new EmpresaResponse();
        r3.setId(3L);

        when(empresaRepository.findAll()).thenReturn(List.of(e1, e2, e3));
        when(empresaMapper.toResponse(e1)).thenReturn(r1);
        when(empresaMapper.toResponse(e2)).thenReturn(r2);
        when(empresaMapper.toResponse(e3)).thenReturn(r3);

        Page<EmpresaResponse> page1 = listEmpresasUseCase.execute(PageRequest.of(0, 2));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(3);
        assertThat(page1.getTotalPages()).isEqualTo(2);
    }
}
