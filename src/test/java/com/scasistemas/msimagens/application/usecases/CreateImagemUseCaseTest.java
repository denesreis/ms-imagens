package com.scasistemas.msimagens.application.usecases;

import com.scasistemas.msimagens.TestSecurityHelper;
import com.scasistemas.msimagens.application.dto.imagem.ImagemResponse;
import com.scasistemas.msimagens.application.dto.imagem.ImagemUploadRequest;
import com.scasistemas.msimagens.application.mappers.ImagemMapper;
import com.scasistemas.msimagens.domain.entities.AuditLog;
import com.scasistemas.msimagens.domain.entities.Imagem;
import com.scasistemas.msimagens.domain.entities.Produto;
import com.scasistemas.msimagens.domain.enums.StatusImagemEnum;
import com.scasistemas.msimagens.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msimagens.domain.exceptions.ResourceNotFoundException;
import com.scasistemas.msimagens.domain.repositories.IAuditLogRepository;
import com.scasistemas.msimagens.domain.repositories.IImagemRepository;
import com.scasistemas.msimagens.domain.repositories.IProdutoRepository;
import com.scasistemas.msimagens.domain.services.CloudflareUploadResult;
import com.scasistemas.msimagens.domain.services.ICloudflareImageService;
import com.scasistemas.msimagens.infrastructure.cloudflare.ImageCompressionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateImagemUseCase - Testes unitários")
class CreateImagemUseCaseTest {

    @Mock
    private IImagemRepository imagemRepository;
    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private IAuditLogRepository auditLogRepository;
    @Mock
    private ICloudflareImageService cloudflareImageService;
    @Mock
    private ImageCompressionService imageCompressionService;
    @Mock
    private ImagemMapper imagemMapper;
    @Mock
    private MultipartFile arquivo;

    @InjectMocks
    private CreateImagemUseCase createImagemUseCase;

    private static final String PRODUTO_ID = "produto-uuid-001";

    @BeforeEach
    void setUp() {
        TestSecurityHelper.mockAdminContext();
    }

    @AfterEach
    void tearDown() {
        TestSecurityHelper.clearContext();
    }

    private ImagemUploadRequest buildUploadRequest() {
        ImagemUploadRequest req = new ImagemUploadRequest();
        req.setIdProduto(PRODUTO_ID);
        req.setTipoArmazenamento(TipoArmazenamentoEnum.ABERTO);
        req.setIdEmpresa(1L);
        return req;
    }

    private Imagem buildImagemPendente() {
        return Imagem.builder()
                .id("imagem-uuid-001")
                .idProduto(PRODUTO_ID)
                .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                .filename("foto.jpg")
                .status(StatusImagemEnum.PENDENTE)
                .build();
    }

    @Test
    @DisplayName("Deve criar imagem com status PENDENTE → ATIVO após upload bem-sucedido")
    void deveCriarImagemPendenteEConfirmarUploadParaAtivo() throws IOException {
        ImagemUploadRequest request = buildUploadRequest();
        Imagem pendente = buildImagemPendente();

        Imagem imagemComDados = buildImagemPendente();
        imagemComDados.confirmarUpload("cf-img-id", "https://cf.url/cf-img-id/public");

        ImagemResponse response = new ImagemResponse();
        response.setId("imagem-uuid-001");
        response.setStatus(StatusImagemEnum.ATIVO);

        Produto produto = Produto.builder().id(PRODUTO_ID).build();

        when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.of(produto));
        when(arquivo.getOriginalFilename()).thenReturn("foto.jpg");
        when(arquivo.getContentType()).thenReturn("image/jpeg");
        when(arquivo.getBytes()).thenReturn(new byte[] { 1, 2, 3 });
        when(imageCompressionService.compress(any(), any(), any())).thenReturn(new byte[] { 1, 2, 3 });
        when(imagemRepository.save(any(Imagem.class))).thenAnswer(inv -> {
            Imagem i = inv.getArgument(0);
            if (i.getId() == null)
                i.setId("imagem-uuid-001");
            return i;
        });
        when(cloudflareImageService.uploadImage(any(), any(), any()))
                .thenReturn(CloudflareUploadResult.success("cf-img-id", "https://cf.url/cf-img-id/public", "foto.jpg"));
        when(imagemMapper.toResponse(any(Imagem.class))).thenReturn(response);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        ImagemResponse result = createImagemUseCase.execute(arquivo, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(StatusImagemEnum.ATIVO);
        verify(cloudflareImageService).uploadImage(any(), eq("foto.jpg"), eq("image/jpeg"));
    }

    @Test
    @DisplayName("Deve marcar imagem com status ERRO quando upload falha na Cloudflare")
    void deveMarcarImagemComoErroQuandoUploadFalha() throws IOException {
        ImagemUploadRequest request = buildUploadRequest();

        ImagemResponse responseErro = new ImagemResponse();
        responseErro.setStatus(StatusImagemEnum.ERRO);

        when(produtoRepository.findById(PRODUTO_ID))
                .thenReturn(Optional.of(Produto.builder().id(PRODUTO_ID).build()));
        when(arquivo.getOriginalFilename()).thenReturn("foto.jpg");
        when(arquivo.getContentType()).thenReturn("image/jpeg");
        when(arquivo.getBytes()).thenReturn(new byte[] { 1, 2, 3 });
        when(imageCompressionService.compress(any(), any(), any())).thenReturn(new byte[] { 1, 2, 3 });
        when(imagemRepository.save(any(Imagem.class))).thenAnswer(inv -> {
            Imagem i = inv.getArgument(0);
            if (i.getId() == null)
                i.setId("imagem-uuid-001");
            return i;
        });
        when(cloudflareImageService.uploadImage(any(), any(), any()))
                .thenReturn(CloudflareUploadResult.error("foto.jpg", "Timeout na API Cloudflare"));
        when(imagemMapper.toResponse(argThat(i -> i.getStatus() == StatusImagemEnum.ERRO)))
                .thenReturn(responseErro);
        when(auditLogRepository.save(any())).thenReturn(null);

        ImagemResponse result = createImagemUseCase.execute(arquivo, request);

        assertThat(result.getStatus()).isEqualTo(StatusImagemEnum.ERRO);

        // Verificar que save foi chamado na segunda vez com status ERRO
        ArgumentCaptor<Imagem> captor = ArgumentCaptor.forClass(Imagem.class);
        verify(imagemRepository, times(2)).save(captor.capture());
        Imagem imagemFinal = captor.getAllValues().get(1);
        assertThat(imagemFinal.getStatus()).isEqualTo(StatusImagemEnum.ERRO);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para produto inexistente")
    void deveLancarExcecaoParaProdutoInexistente() {
        ImagemUploadRequest request = buildUploadRequest();
        when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createImagemUseCase.execute(arquivo, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(PRODUTO_ID);

        verify(imagemRepository, never()).save(any());
    }
}
