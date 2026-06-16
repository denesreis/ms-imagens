package com.scasistemas.msbluedot.security;

import com.scasistemas.msbluedot.entity.Usuario;
import com.scasistemas.msbluedot.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service("newCustomUserDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNomeIgnoreCase(username)
                .orElseThrow(() -> {
                    log.debug("[UserDetails] Usuário '{}' não encontrado", username);
                    return new UsernameNotFoundException("Usuário não encontrado: " + username);
                });

        log.debug("[UserDetails] Usuário '{}' carregado | role={} | bloqueado={}",
                usuario.getNome(), usuario.getRole(), usuario.estaBloqueado());

        return new UserPrincipal(usuario);
    }
}
