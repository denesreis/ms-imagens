package com.scasistemas.msimagens.infrastructure.persistence;

import com.scasistemas.msimagens.JpaAuditingTestConfig;
import com.scasistemas.msimagens.infrastructure.persistence.entities.EmpresaEntity;
import com.scasistemas.msimagens.infrastructure.persistence.repositories.EmpresaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingTestConfig.class)
@DisplayName("EmpresaJpaRepository - Testes de integração JPA")
class EmpresaRepositoryIntegrationTest {

    @Autowired
    private EmpresaJpaRepository empresaRepository;

    private EmpresaEntity buildEmpresa(String codigoErp, String nome) {
        return EmpresaEntity.builder()
                .codigoErp(codigoErp)
                .nome(nome)
                .build();
    }

    @Test
    @DisplayName("Deve salvar e recuperar empresa pelo id")
    void deveSalvarERecuperarEmpresaPeloId() {
        EmpresaEntity empresa = buildEmpresa("ERP-001", "Empresa Teste");
        EmpresaEntity salva = empresaRepository.save(empresa);

        Optional<EmpresaEntity> encontrada = empresaRepository.findById(salva.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getCodigoErp()).isEqualTo("ERP-001");
        assertThat(encontrada.get().getNome()).isEqualTo("Empresa Teste");
        assertThat(encontrada.get().getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve preencher campos de auditoria automaticamente")
    void devePreencherCamposDeAuditoria() {
        EmpresaEntity salva = empresaRepository.save(buildEmpresa("ERP-002", "Empresa Auditada"));

        assertThat(salva.getDataCriacao()).isNotNull();
        assertThat(salva.getDataAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar empresa por codigoErp")
    void deveBuscarEmpresaPorCodigoErp() {
        empresaRepository.save(buildEmpresa("ERP-003", "Empresa ERP"));

        Optional<EmpresaEntity> encontrada = empresaRepository.findByCodigoErp("ERP-003");

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNome()).isEqualTo("Empresa ERP");
    }

    @Test
    @DisplayName("Deve retornar empty ao buscar codigoErp inexistente")
    void deveRetornarEmptyParaCodigoErpInexistente() {
        Optional<EmpresaEntity> resultado = empresaRepository.findByCodigoErp("ERP-NAO-EXISTE");
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve aplicar soft delete — registro não deve aparecer nos selects")
    void deveAplicarSoftDeleteEOcultarRegistro() {
        EmpresaEntity empresa = empresaRepository.save(buildEmpresa("ERP-DEL", "Empresa Delete"));
        Long id = empresa.getId();

        empresaRepository.deleteById(id);
        empresaRepository.flush();

        Optional<EmpresaEntity> aposDelete = empresaRepository.findById(id);
        assertThat(aposDelete).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty ao buscar por codigoErp com soft delete aplicado")
    void deveRetornarEmptyBuscarCodigoErpAposSoftDelete() {
        EmpresaEntity empresa = empresaRepository.save(buildEmpresa("ERP-SD", "Empresa SD"));
        empresaRepository.deleteById(empresa.getId());
        empresaRepository.flush();

        Optional<EmpresaEntity> resultado = empresaRepository.findByCodigoErp("ERP-SD");
        assertThat(resultado).isEmpty();
    }
}
