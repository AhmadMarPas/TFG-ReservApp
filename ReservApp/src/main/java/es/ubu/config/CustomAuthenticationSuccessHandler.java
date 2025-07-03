package es.ubu.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Manejador personalizado para el éxito de autenticación.
 * Se encarga de establecer el usuario en SessionData cuando la autenticación es exitosa.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final SessionData sessionData;
    private final UsuarioService usuarioService;

    public CustomAuthenticationSuccessHandler(SessionData sessionData, UsuarioService usuarioService) {
        this.sessionData = sessionData;
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        log.info("Usuario autenticado exitosamente: {}", username);
        
        // Buscar el usuario en la base de datos y establecerlo en SessionData
        Usuario usuario = usuarioService.findUsuarioById(username);
        if (usuario != null) {
            sessionData.setUsuario(usuario);
            log.info("Usuario establecido en SessionData: {}", usuario.getNombre());
        } else {
            log.warn("No se pudo encontrar el usuario en la base de datos: {}", username);
        }
        
        // Redirigir al menú principal
        response.sendRedirect("/menuprincipal");
    }
}