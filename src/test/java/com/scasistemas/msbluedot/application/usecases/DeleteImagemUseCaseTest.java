package com.scasistemas.msbluedot.application.usecases;

import com.scasistemas.msbluedot.TestSecurityHelper;
import com.scasistemas.msbluedot.domain.entities.AuditLog;
import com.scasistemas.msbluedot.domain.entities.Imagem;
import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msbluedot.domain.exceptions.UnauthorizedException;
import com.scasistemas.msbluedot.domain.repositories.IAuditLogRepository;
import com.scasistemas.msbluedot.domain.repositories.IImagemRepository;
import com.scasistemas.msbluedot.domain.services.ICloudflareImageService;
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
@DisplayName("DeleteImagemUseCase - Testes unitários")
class DeleteImagemUseCaseTest {

    @Mock
    private IImagemRepository imagemRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private ICloudflareImageService cloudflareImageService;

    @InjectMocks
    private DeleteImagemUseCase deleteImagemUseCase;

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    private Imagem buildImagem() {
        return Imagem.builder()
                .id("img-001")
                .idProduto("prod-001")
                .idEmpresa(1L)
                .idImagemCloudflare("cf-img-001")
                .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                .status(StatusImagemEnum.ATIVO)
                .build();
    }

    @Test
    @DisplayName("Deve deletar imagem com sucesso incluindo Cloudflare")
    void deveDeletarImagemComSucesso() {
        Imagem imagem = buildImagem();
        when(imagemRepository.findById("img-001")).thenReturn(Optional.of(imagem));
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        deleteImagemUseCase.execute("img-001");

        verify(cloudflareImageService).deleteImage("cf-img-001");
        verify(imagemRepository).deleteById("img-001");
        verify(auditLogRepository).save(argThat(log -> "DELETAR_IMAGEM".equals(log.getAcao())));
    }

    @Test
    @DisplayName("Deve executar soft delete mesmo se Cloudflare falhar (fail-safe)")
    void deveDeletarMesmoSeCloudflareFalhar() {
        Imagem imagem = buildImagem();
        when(imagemRepository.findById("img-001")).thenReturn(Optional.of(imagem));
        doThrow(new RuntimeException("Cloudflare timeout"))
                .when(cloudflareImageService).deleteImage("cf-img-001");
        when(auditLogRepository.save(any())).thenReturn(null);

        deleteImagemUseCase.execute("img-001");

        verify(imagemRepository).deleteById("img-001");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Deve pular deleção na Cloudflare quando não há idImagemCloudflare")
    void devePularCloudflareQuandoSemId() {
        Imagem imagem = buildImagem();
        imagem.setIdImagemCloudflare(null);
        when(imagemRepository.findById("img-001")).thenReturn(Optional.of(imagem));
        when(auditLogRepository.save(any())).thenReturn(null);

        deleteImagemUseCase.execute("img-001");

        verify(cloudflareImageService, never()).deleteImage(any());
        verify(imagemRepository).deleteById("img-001");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para imagem inexistente")
    void deveLancarExcecaoParaImagemInexistente() {
        when(imagemRepository.findById("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteImagemUseCase.execute("nao-existe"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(imagemRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando USUARIO tenta deletar imagem de outra empresa")
    void deveLancarExcecaoParaUsuarioDeOutraEmpresa() {
        TestSecurityHelper.clearContext();
        TestSecurityHelper.mockUsuarioContext(); // idEmpresa=1
        Imagem imagem = buildImagem();
        imagem.setIdEmpresa(99L);

        when(imagemRepository.findById("img-001")).thenReturn(Optional.of(imagem));

        assertThatThrownBy(() -> deleteImagemUseCase.execute("img-001"))
                .isInstanceOf(UnauthorizedException.class);

        verify(imagemRepository, never()).deleteById(any());
    }
}

