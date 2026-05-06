package com.scasistemas.msbluedot.infrastructure.web.controllers;

import com.scasistemas.msbluedot.domain.entities.Usuario;
import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import com.scasistemas.msbluedot.domain.enums.StatusImagemEnum;
import com.scasistemas.msbluedot.domain.enums.TipoArmazenamentoEnum;
import com.scasistemas.msbluedot.domain.services.ICloudflareImageService;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.ImagemEntity;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.ProdutoEntity;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.UsuarioEntity;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.ImagemJpaRepository;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.ProdutoJpaRepository;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.UsuarioJpaRepository;
import com.scasistemas.msbluedot.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ImagemController - Testes de integração")
class ImagemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;
    @Autowired
    private ProdutoJpaRepository produtoJpaRepository;
    @Autowired
    private ImagemJpaRepository imagemJpaRepository;

    @MockBean
    @SuppressWarnings("unused")
    private ICloudflareImageService cloudflareImageService;

    private static final String CODIGO_EAN_IT = "1112223334441";

    private String usuarioToken;
    private UsuarioEntity userEntity;
    private ProdutoEntity produto;

    @BeforeEach
    void setUp() {
        userEntity = usuarioJpaRepository.save(UsuarioEntity.builder()
                .nome("img.it.user." + System.nanoTime())
                .senha("$2a$12$hash_nao_usado")
                .role(RoleEnum.USUARIO)
                .idEmpresa(1L)
                .build());

        usuarioToken = jwtTokenProvider.generateAccessToken(
                Usuario.builder().id(userEntity.getId())
                        .nome(userEntity.getNome())
                        .role(RoleEnum.USUARIO)
                        .idEmpresa(1L).build());

        produto = produtoJpaRepository.save(ProdutoEntity.builder()
                .descricao("Produto para Teste de Imagem")
                .codigoEan(CODIGO_EAN_IT)
                .idEmpresa(1L)
                .build());
    }

    @AfterEach
    void tearDown() {
        imagemJpaRepository.deleteAll();
        produtoJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/imagens/ean/{ean} SEM JWT deve retornar 200 (endpoint público)")
    void getImagensPorEan_semJwt_deveRetornar200() throws Exception {
        mockMvc.perform(get("/api/v1/imagens/ean/{codigoEan}", CODIGO_EAN_IT))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/imagens/ean/{ean} deve retornar lista de imagens ABERTO+ATIVO")
    void getImagensPorEan_deveRetornarApenasImagensAbertas() throws Exception {
        // ATIVO + ABERTO — deve aparecer
        imagemJpaRepository.save(ImagemEntity.builder()
                .idProduto(produto.getId())
                .idEmpresa(1L)
                .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                .filename("foto1.jpg")
                .status(StatusImagemEnum.ATIVO)
                .url("https://cf.url/img1/public")
                .idImagemCloudflare("img1")
                .build());

        // PENDENTE + ABERTO — NÃO deve aparecer
        imagemJpaRepository.save(ImagemEntity.builder()
                .idProduto(produto.getId())
                .idEmpresa(1L)
                .tipoArmazenamento(TipoArmazenamentoEnum.ABERTO)
                .filename("foto2.jpg")
                .status(StatusImagemEnum.PENDENTE)
                .build());

        // ATIVO + PRIVADO — NÃO deve aparecer
        imagemJpaRepository.save(ImagemEntity.builder()
                .idProduto(produto.getId())
                .idEmpresa(1L)
                .tipoArmazenamento(TipoArmazenamentoEnum.PRIVADO)
                .filename("foto3.jpg")
                .status(StatusImagemEnum.ATIVO)
                .url("https://cf.url/img3/public")
                .idImagemCloudflare("img3")
                .build());

        mockMvc.perform(get("/api/v1/imagens/ean/{codigoEan}", CODIGO_EAN_IT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].url").value("https://cf.url/img1/public"));
    }

    @Test
    @DisplayName("GET /api/v1/imagens/ean/{ean} deve retornar lista vazia para EAN sem imagens")
    void getImagensPorEan_semImagens_deveRetornarListaVazia() throws Exception {
        mockMvc.perform(get("/api/v1/imagens/ean/{codigoEan}", "9999999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/imagens/produto/{id} SEM JWT deve retornar 401")
    void getImagensPorProduto_semJwt_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/imagens/produto/{idProduto}", produto.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/imagens/produto/{id} com JWT deve retornar 200")
    void getImagensPorProduto_comJwt_deveRetornar200() throws Exception {
        mockMvc.perform(get("/api/v1/imagens/produto/{idProduto}", produto.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + usuarioToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/imagens/{id} com JWT para imagem inexistente deve retornar 404")
    void getImagemPorId_inexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/api/v1/imagens/{id}", "id-que-nao-existe")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + usuarioToken))
                .andExpect(status().isNotFound());
    }
}

