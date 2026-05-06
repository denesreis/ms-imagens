package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.TestSecurityHelper;
import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IUsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("DeleteUsuarioUseCase - Testes unitários")
class DeleteUsuarioUseCaseTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;

    @InjectMocks
    private DeleteUsuarioUseCase deleteUsuarioUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso e registrar audit log")
    void deveDeletarUsuarioComSucesso() {
        Usuario usuario = Usuario.builder().id(5L).nome("user5").idEmpresa(1L).role(RoleEnum.USUARIO).build();
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        deleteUsuarioUseCase.execute(5L);

        verify(usuarioRepository).deleteById(5L);
        verify(auditLogRepository).save(argThat(log -> "DELETAR_USUARIO".equals(log.getAcao())));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para usuário inexistente")
    void deveLancarExcecaoParaUsuarioInexistente() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteUsuarioUseCase.execute(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).deleteById(any());
    }
}

