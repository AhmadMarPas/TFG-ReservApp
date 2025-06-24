package es.ubu.reservapp.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
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
	 * Constructor del controlador que inyecta los datos de la sesión.
	 * 
	 * @param sessionData
	 */
    public HomeController(SessionData sessionData) {
    	this.sessionData = sessionData;
    }

    @GetMapping("/")
    public String home(Model model) {
    	model.addAttribute("usuario", new Usuario());
        return "login";
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
        List<Establecimiento> establecimientos = usuario.getEstablecimiento();
        model.addAttribute("establecimientos", establecimientos);
        return "reservas/misreservas";
    }
}
