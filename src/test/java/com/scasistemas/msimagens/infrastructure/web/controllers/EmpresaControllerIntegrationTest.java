package com.scasistemas.msimagens.infrastructure.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scasistemas.msimagens.application.dto.empresa.EmpresaRequest;
import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import com.scasistemas.msimagens.domain.services.ICloudflareImageService;
import com.scasistemas.msimagens.infrastructure.persistence.entities.EmpresaEntity;
import com.scasistemas.msimagens.infrastructure.persistence.repositories.EmpresaJpaRepository;
import com.scasistemas.msimagens.infrastructure.security.UserPrincipal;
import com.scasistemas.msimagens.infrastructure.security.WithMockUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EmpresaController - Testes de integracao")
class EmpresaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmpresaJpaRepository empresaJpaRepository;

    @MockBean
    @SuppressWarnings("unused")
    private ICloudflareImageService cloudflareImageService;

    @AfterEach
    void tearDown() {
        empresaJpaRepository.deleteAll();
    }

    private EmpresaRequest buildEmpresaRequest(String codigoErp) {
        EmpresaRequest req = new EmpresaRequest();
        req.setCodigoErp(codigoErp);
        req.setNome("Empresa Integracao " + codigoErp);
        return req;
    }

    @Test
    @DisplayName("POST /api/v1/empresas com ADMINISTRADOR deve retornar 201")
    @WithMockUserPrincipal(role = RoleEnum.ADMINISTRADOR, nome = "admin.test")
    void postEmpresa_comAdminRole_deveRetornar201() throws Exception {
        EmpresaRequest request = buildEmpresaRequest("ERP-IT-01");
        mockMvc.perform(post("/api/v1/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoErp").value("ERP-IT-01"))
                .andExpect(jsonPath("$.nome").value("Empresa Integracao ERP-IT-01"));
    }

    @Test
    @DisplayName("POST /api/v1/empresas sem autenticacao deve retornar 401")
    void postEmpresa_semAutenticacao_deveRetornar401() throws Exception {
        EmpresaRequest request = buildEmpresaRequest("ERP-IT-02");
        mockMvc.perform(post("/api/v1/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/empresas com role USUARIO deve retornar 403")
    @WithMockUserPrincipal(role = RoleEnum.USUARIO)
    void postEmpresa_comRoleUsuario_deveRetornar403() throws Exception {
        EmpresaRequest request = buildEmpresaRequest("ERP-IT-03");
        mockMvc.perform(post("/api/v1/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/empresas/{id} com role USUARIO deve retornar 200")
    void getEmpresaById_comRoleUsuario_deveRetornar200() throws Exception {
        EmpresaEntity empresa = empresaJpaRepository.save(EmpresaEntity.builder()
                .codigoErp("ERP-IT-GET")
                .nome("Empresa para GET")
                .build());

        // UserPrincipal with idEmpresa = empresa.getId() to pass ownership check
        Usuario usuario = Usuario.builder()
                .id(99L).nome("test.user.get")
                .role(RoleEnum.USUARIO)
                .idEmpresa(empresa.getId())
                .ativo(true).senha("N/A").build();
        UserPrincipal principal = new UserPrincipal(usuario);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        mockMvc.perform(get("/api/v1/empresas/{id}", empresa.getId())
                .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(empresa.getId()))
                .andExpect(jsonPath("$.codigoErp").value("ERP-IT-GET"));
    }

    @Test
    @DisplayName("GET /api/v1/empresas/{id} sem autenticacao deve retornar 401")
    void getEmpresaById_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/empresas/{id}", 999L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/empresas com codigoErp duplicado deve retornar 422")
    @WithMockUserPrincipal(role = RoleEnum.ADMINISTRADOR, nome = "admin.dup")
    void postEmpresa_codigoErpDuplicado_deveRetornar422() throws Exception {
        empresaJpaRepository.save(EmpresaEntity.builder()
                .codigoErp("ERP-DUP")
                .nome("Empresa Existente")
                .build());
        EmpresaRequest request = buildEmpresaRequest("ERP-DUP");
        mockMvc.perform(post("/api/v1/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}