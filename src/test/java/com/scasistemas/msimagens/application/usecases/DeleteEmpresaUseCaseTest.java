package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteEmpresaUseCase - Testes unitários")
class DeleteEmpresaUseCaseTest {

    @Mock
    private IEmpresaRepository empresaRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;

    @InjectMocks
    private DeleteEmpresaUseCase deleteEmpresaUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve deletar empresa com sucesso e registrar audit log")
    void deveDeletarEmpresaComSucesso() {
        when(empresaRepository.existsById(1L)).thenReturn(true);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        deleteEmpresaUseCase.execute(1L);

        verify(empresaRepository).deleteById(1L);
        verify(auditLogRepository).save(argThat(log -> "DELETAR_EMPRESA".equals(log.getAcao())));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para empresa inexistente")
    void deveLancarExcecaoParaEmpresaInexistente() {
        when(empresaRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> deleteEmpresaUseCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(empresaRepository, never()).deleteById(any());
    }
}
