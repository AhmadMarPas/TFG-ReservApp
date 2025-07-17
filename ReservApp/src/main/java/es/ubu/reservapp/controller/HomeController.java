package es.ubu.reservapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la página de inicio y el menú principal.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Controller
public class HomeController {

	/**
	 * Datos de la sesión.
	 */
	private SessionData sessionData;
	/**
	 * Servicio de Usuario
	 */
    private final UsuarioService usuarioService;

	/**
	 * Constructor del controlador que inyecta los datos de la sesión.
	 * 
	 * @param sessionData
	 * @param usuarioService
	 */
    public HomeController(SessionData sessionData, UsuarioService usuarioService) {
    	this.sessionData = sessionData;
    	this.usuarioService = usuarioService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/menuprincipal";
    }
    
    @GetMapping("/menuprincipal")
    public String menuprincipal() {
        return "menuprincipal";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/misreservas")
    public String misReservas(Model model) {
        Usuario usuario = sessionData.getUsuario();
		if (usuario == null) {
			log.warn("Usuario no autenticado al intentar acceder a mis reservas.");
			return "redirect:/login";
		}
		usuarioService.obtenerEstablecimientosUsuario(usuario, model);
        return "reservas/misreservas";
    }
}
