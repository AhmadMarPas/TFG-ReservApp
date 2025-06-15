package es.ubu.reservapp.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;

/**
 * Controlador para la página de inicio y el menú principal.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
public class HomeController {

	/**
	 * Datos de la sesión.
	 */
	private SessionData sessionData;

	private final UsuarioService usuarioService;

    public HomeController(SessionData sessionData, UsuarioService usuarioService) {
    	this.sessionData = sessionData;
        this.usuarioService = usuarioService;
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
        List<Establecimiento> establecimientos = usuario.getEstablecimiento();
        System.out.println("Establecimientos del usuario: " + establecimientos);
        model.addAttribute("establecimientos", establecimientos);
        return "reservas/misreservas";
    }
}
