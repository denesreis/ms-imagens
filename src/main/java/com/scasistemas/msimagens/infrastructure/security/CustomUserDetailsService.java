package com.scasistemas.msimagens.infrastructure.security;

import com.scasistemas.msimagens.domain.entities.Usuario;
import com.scasistemas.msimagens.domain.repositories.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementação de {@link UserDetailsService} para integração com o
 * Spring Security.
 *
 * <p>
 * Carrega o usuário pelo nome (username) a partir do repositório de domínio
 * e retorna um {@link UserPrincipal} que encapsula a entidade de domínio.
 * </p>
 *
 * <p>
 * O bloqueio de conta ({@code isAccountNonLocked()}) é verificado pelo
 * próprio Spring Security após este método retornar, com base no campo
 * {@code bloqueadoAte} do {@link UserPrincipal}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUsuarioRepository usuarioRepository;

    /**
     * Carrega o usuário pelo nome (username).
     *
     * @param username nome do usuário (campo {@code nome} na tb_usuario)
     * @return {@link UserPrincipal} com dados do usuário
     * @throws UsernameNotFoundException se o usuário não for encontrado ou estiver
     *                                   inativo
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNome(username)
                .orElseThrow(() -> {
                    log.debug("[UserDetails] Usuário '{}' não encontrado", username);
                    return new UsernameNotFoundException("Usuário não encontrado: " + username);
                });

        log.debug("[UserDetails] Usuário '{}' carregado | role={} | bloqueado={}",
                usuario.getNome(), usuario.getRole(), usuario.estaBloqueado());

        return new UserPrincipal(usuario);
    }
}
