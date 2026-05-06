package com.scasistemas.msbluedot.infrastructure.persistence;

import com.scasistemas.msbluedot.JpaAuditingTestConfig;
import com.scasistemas.msbluedot.domain.enums.RoleEnum;
import com.scasistemas.msbluedot.infrastructure.persistence.entities.UsuarioEntity;
import com.scasistemas.msbluedot.infrastructure.persistence.repositories.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaAuditingTestConfig.class)
@DisplayName("UsuarioJpaRepository - Testes de integração JPA")
class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    private UsuarioEntity buildUsuario(String nome, Long idEmpresa) {
        return UsuarioEntity.builder()
                .nome(nome)
                .senha("$2a$12$hash_teste_padrao")
                .role(RoleEnum.USUARIO)
                .idEmpresa(idEmpresa)
                .build();
    }

    @Test
    @DisplayName("Deve salvar e recuperar usuário pelo id")
    void deveSalvarERecuperarUsuarioPeloId() {
        UsuarioEntity usuario = buildUsuario("joao.silva", 1L);
        UsuarioEntity salvo = usuarioRepository.save(usuario);

        Optional<UsuarioEntity> encontrado = usuarioRepository.findById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("joao.silva");
        assertThat(encontrado.get().getTentativasLogin()).isEqualTo(0);
        assertThat(encontrado.get().getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar usuário por nome")
    void deveBuscarUsuarioPorNome() {
        usuarioRepository.save(buildUsuario("maria.santos", 1L));

        Optional<UsuarioEntity> encontrado = usuarioRepository.findByNomeIgnoreCase("maria.santos");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getRole()).isEqualTo(RoleEnum.USUARIO);
    }

    @Test
    @DisplayName("Deve retornar empty ao buscar nome inexistente")
    void deveRetornarEmptyParaNomeInexistente() {
        Optional<UsuarioEntity> resultado = usuarioRepository.findByNomeIgnoreCase("nao.existe");
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve persistir campos tentativasLogin e bloqueadoAte")
    void devePersistirCamposDeBloqueio() {
        UsuarioEntity usuario = buildUsuario("carlos.test", 1L);
        usuario.setTentativasLogin(3);
        usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(30));
        UsuarioEntity salvo = usuarioRepository.saveAndFlush(usuario);

        Optional<UsuarioEntity> encontrado = usuarioRepository.findById(salvo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTentativasLogin()).isEqualTo(3);
        assertThat(encontrado.get().getBloqueadoAte()).isNotNull();
    }

    @Test
    @DisplayName("existsByNomeIgnoringSoftDelete deve retornar true mesmo após soft delete")
    void deveRetornarTrueParaNomeExistenteAposSoftDelete() {
        UsuarioEntity u = usuarioRepository.save(buildUsuario("soft.delete.user", 1L));
        usuarioRepository.deleteById(u.getId());
        usuarioRepository.flush();

        // Nome ainda existe na tabela (soft delete não remove fisicamente)
        boolean existe = usuarioRepository.existsByNomeIgnoringSoftDelete("soft.delete.user");
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("findByNome deve retornar empty após soft delete")
    void deveFindByNomeRetornarEmptyAposSoftDelete() {
        UsuarioEntity u = usuarioRepository.save(buildUsuario("usuario.delete", 1L));
        usuarioRepository.deleteById(u.getId());
        usuarioRepository.flush();

        Optional<UsuarioEntity> resultado = usuarioRepository.findByNomeIgnoreCase("usuario.delete");
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar usuários de uma empresa por página")
    void deveListarUsuariosPorEmpresaPaginado() {
        usuarioRepository.save(buildUsuario("user1.empresa2", 2L));
        usuarioRepository.save(buildUsuario("user2.empresa2", 2L));
        usuarioRepository.save(buildUsuario("user3.empresa3", 3L));

        Page<UsuarioEntity> pagina = usuarioRepository.findByIdEmpresa(2L, PageRequest.of(0, 10));

        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getContent()).allMatch(u -> u.getIdEmpresa().equals(2L));
    }
}

