package es.ubu.reservapp.controller;

import java.util.Collection;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import es.ubu.reservapp.service.UsuarioService;

/**
 * Controlador de Usuario.
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/users")
public class UsuarioController {

	private final UsuarioService usuarioService;

	/**
	 * Constructor de la clase.
	 * 
	 * @param usuarioService Servicio de la entidad Usuario.
	 */
	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}
	
	/**
	 * Método que muestra la información de la cuenta.
	 * 
	 * @return Información de la cuenta.
	 */
	@GetMapping("/account")
	public ResponseEntity<Map<String, Object>> getUserInfo() {
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		return ResponseEntity.ok(Map.of("info", "Basic Auth", "username", authentication.getName(), "authorities", authorities));
	}

	/**
	 * Método que muestra la página de inicio.
	 * 
	 * @return Página de inicio.
	 */
	@GetMapping("/admin")
	public String admindata() {
		return "admin page";
	}

	/**
	 * Método que muestra la página de usuario.
	 * 
	 * @return Página de usuario.
	 */
	@GetMapping("/user")
	public String userpage() {
		return "user page";
	}

}
