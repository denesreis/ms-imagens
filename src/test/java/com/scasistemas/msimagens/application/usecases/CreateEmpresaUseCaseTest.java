package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.empresa.EmpresaRequest;
import com.scasistemas.msimagens.application.dto.empresa.EmpresaResponse;
import com.scasistemas.msimagens.application.mappers.EmpresaMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Empresa;
import com.scasistemas.msimagens.domain.exceptions.BusinessException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateEmpresaUseCase - Testes unitários")
class CreateEmpresaUseCaseTest {

    @Mock
    private IEmpresaRepository empresaRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private CreateEmpresaUseCase createEmpresaUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    private EmpresaRequest buildRequest() {
        EmpresaRequest req = new EmpresaRequest();
        req.setCodigoErp("ERP-001");
        req.setNome("Empresa Teste");
        return req;
    }

    @Test
    @DisplayName("Deve criar empresa com sucesso")
    void deveCriarEmpresaComSucesso() {
        EmpresaRequest request = buildRequest();

        Empresa empresa = Empresa.builder()
                .codigoErp("ERP-001")
                .nome("Empresa Teste")
                .build();

        Empresa salva = Empresa.builder()
                .id(1L)
                .codigoErp("ERP-001")
                .nome("Empresa Teste")
                .build();

        EmpresaResponse response = new EmpresaResponse();
        response.setId(1L);
        response.setCodigoErp("ERP-001");
        response.setNome("Empresa Teste");

        when(empresaRepository.findByCodigoErp("ERP-001")).thenReturn(Optional.empty());
        when(empresaMapper.fromRequest(request)).thenReturn(empresa);
        when(empresaRepository.save(empresa)).thenReturn(salva);
        when(empresaMapper.toResponse(salva)).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        EmpresaResponse result = createEmpresaUseCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCodigoErp()).isEqualTo("ERP-001");
        verify(empresaRepository).save(empresa);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao criar empresa com codigoErp duplicado")
    void deveLancarExcecaoAoCriarComCodigoErpDuplicado() {
        EmpresaRequest request = buildRequest();

        Empresa existente = Empresa.builder().id(99L).codigoErp("ERP-001").build();
        when(empresaRepository.findByCodigoErp("ERP-001")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> createEmpresaUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ERP-001");

        verify(empresaRepository, never()).save(any());
    }
}
