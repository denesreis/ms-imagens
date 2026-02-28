package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msimagens.application.mappers.EmpresaMapper;
import com.scasistemas.msimagens.domain.entities.Empresa;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.exceptions.UnauthorizedException;
import com.scasistemas.msimagens.domain.repositories.IEmpresaRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetEmpresaUseCase - Testes unitários")
class GetEmpresaUseCaseTest {

    @Mock
    private IEmpresaRepository empresaRepository;
    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private GetEmpresaUseCase getEmpresaUseCase;

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve retornar empresa quando ADMIN busca qualquer empresa")
    void deveRetornarEmpresaParaAdmin() {
        TestSecurityHelper.mockAdminContext();
        Empresa empresa = Empresa.builder().id(2L).nome("Empresa 2").build();
        EmpresaResponse response = new EmpresaResponse();
        response.setId(2L);
        response.setNome("Empresa 2");

        when(empresaRepository.findById(2L)).thenReturn(Optional.of(empresa));
        when(empresaMapper.toResponse(empresa)).thenReturn(response);

        EmpresaResponse result = getEmpresaUseCase.execute(2L);

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve retornar empresa quando USUARIO busca sua própria empresa")
    void deveRetornarEmpresaParaUsuarioDaMesmaEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Empresa empresa = Empresa.builder().id(1L).nome("Empresa 1").build();
        EmpresaResponse response = new EmpresaResponse();
        response.setId(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaMapper.toResponse(empresa)).thenReturn(response);

        EmpresaResponse result = getEmpresaUseCase.execute(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO busca empresa diferente")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Empresa empresa = Empresa.builder().id(99L).nome("Outra Empresa").build();

        when(empresaRepository.findById(99L)).thenReturn(Optional.of(empresa));

        assertThatThrownBy(() -> getEmpresaUseCase.execute(99L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para empresa inexistente")
    void deveLancarExcecaoParaEmpresaInexistente() {
        TestSecurityHelper.mockAdminContext();
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getEmpresaUseCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
