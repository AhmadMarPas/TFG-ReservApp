package es.ubu.config;

import java.io.IOException;
import java.time.LocalDateTime;

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

	/** sessionData: para almacenar el usuario autenticado en la sesión. */
    private final SessionData sessionData;
    /** usuarioService: para manejar operaciones relacionadas con Usuario. */
    private final UsuarioService usuarioService;

    /**
	 * Constructor para inyección de dependencias.
	 * 
	 * @param sessionData  El objeto SessionData para almacenar el usuario autenticado.
	 * @param usuarioService El servicio para manejar operaciones relacionadas con Usuario.
	 */
    public CustomAuthenticationSuccessHandler(SessionData sessionData, UsuarioService usuarioService) {
        this.sessionData = sessionData;
        this.usuarioService = usuarioService;
    }

    @Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        log.info("Usuario autenticado exitosamente: {}", username);
        
        Usuario usuario = usuarioService.findUsuarioById(username);
        if (usuario != null) {
        	sessionData.setUsuario(usuario);
        	log.info("Usuario establecido en SessionData: {}", usuario.getNombre());

        	usuario.setFechaUltimoAcceso(LocalDateTime.now());
            usuarioService.save(usuario);
            log.info("Fecha de último acceso actualizada para usuario: {}", username);
        } else {
            log.warn("No se pudo encontrar el usuario en la base de datos: {}", username);
        }
        
        response.sendRedirect("/menuprincipal");
    }
}