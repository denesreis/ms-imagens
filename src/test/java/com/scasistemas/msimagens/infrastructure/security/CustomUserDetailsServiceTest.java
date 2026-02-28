package com.scasistemas.msimagens.infrastructure.security;

import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.enums.RoleEnum;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService - Testes unitários")
class CustomUserDetailsServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Deve carregar usuário existente e retornar UserPrincipal")
    void deveCarregarUsuarioExistente() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("admin")
                .senha("$2a$12$hash")
                .role(RoleEnum.ADMINISTRADOR)
                .idEmpresa(1L)
                .ativo(true)
                .tentativasLogin(0)
                .build();

        when(usuarioRepository.findByNome("admin")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(UserPrincipal.class);
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities()).isNotEmpty();
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException para usuário inexistente")
    void deveLancarExcecaoParaUsuarioInexistente() {
        when(usuarioRepository.findByNome("naoexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("naoexiste"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("naoexiste");
    }
}
