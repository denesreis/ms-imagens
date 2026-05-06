package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.TestSecurityHelper;
import com.scasistemas.msbluedot.application.dto.empresa.EmpresaRequest;
import com.scasistemas.msbluedot.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msbluedot.application.mappers.EmpresaMapper;
import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Empresa;
import com.scasistemas.msbluedot.domain.exceptions.BusinessException;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IEmpresaRepository;
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
@DisplayName("UpdateEmpresaUseCase - Testes unitários")
class UpdateEmpresaUseCaseTest {

    @Mock
    private IEmpresaRepository empresaRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private UpdateEmpresaUseCase updateEmpresaUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve atualizar empresa com sucesso")
    void deveAtualizarEmpresaComSucesso() {
        Empresa empresa = Empresa.builder().id(1L).codigoErp("ERP-001").nome("Empresa 1").build();
        EmpresaRequest request = new EmpresaRequest();
        request.setCodigoErp("ERP-001");
        request.setNome("Nome Atualizado");
        EmpresaResponse response = new EmpresaResponse();
        response.setId(1L);

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(empresa)).thenReturn(empresa);
        when(empresaMapper.toResponse(empresa)).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        EmpresaResponse result = updateEmpresaUseCase.execute(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(empresa.getNome()).isEqualTo("Nome Atualizado");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao alterar codigoErp para um já existente")
    void deveLancarExcecaoParaCodigoErpDuplicado() {
        Empresa empresa = Empresa.builder().id(1L).codigoErp("ERP-001").nome("Empresa 1").build();
        EmpresaRequest request = new EmpresaRequest();
        request.setCodigoErp("ERP-002");
        request.setNome("Nome");
        Empresa outra = Empresa.builder().id(99L).codigoErp("ERP-002").build();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.findByCodigoErp("ERP-002")).thenReturn(Optional.of(outra));

        assertThatThrownBy(() -> updateEmpresaUseCase.execute(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ERP-002");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para empresa inexistente")
    void deveLancarExcecaoParaEmpresaInexistente() {
        when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateEmpresaUseCase.execute(999L, new EmpresaRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

