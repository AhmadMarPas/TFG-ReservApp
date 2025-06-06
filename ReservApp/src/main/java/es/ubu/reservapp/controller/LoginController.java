package es.ubu.reservapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

/**
 * Controlador de Login.
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
public class LoginController {
	
	private static final String REDIRECT_REGISTRO = "redirect:/registro";
	private static final String REDIRECT_LOGIN = "redirect:/login";
	private static final String REDIRECT_INICIO = "redirect:/menuprincipal";

	private UsuarioService userService;

	/**
	 * Datos de la sesión.
	 */
	private SessionData sessionData;

	/**
	 * Constructor de LoginController.
	 * 
	 * @param sessionData Datos de la sesión
	 */
	public LoginController(UsuarioService userService, SessionData sessionData) {
		this.userService = userService;
		this.sessionData = sessionData;
	}
	
	@GetMapping("/loginer")
    public String loginForm(Model model) {
		model.addAttribute("usuario", new Usuario());
        return "login";
    }
	
	@PostMapping("/loginer")
	public String login(@RequestParam String username, @RequestParam String password, Model model, RedirectAttributes redirectAttributes) {
		log.info("username: " + username);
		log.info("password: " + password);
        Usuario usuario = userService.validateAuthentication(username, password);
		if (usuario != null) {
			// Guardamos el usuario
			model.addAttribute("Usuario", usuario);
			log.info("Valid username and password");
			usuario = (Usuario) model.getAttribute("Usuario");
			log.info("Usuario: " + usuario.getNombre());
			sessionData.setUsuario(usuario);
			return REDIRECT_INICIO;
		} else {
			log.error("Invalid username and password");
			model.addAttribute("error", "Usuario o contraseña incorrectos.");
			redirectAttributes.addFlashAttribute("error", "Usuario o contraseña incorrectos");
			return "login";
		}
	}
	
    @GetMapping("/registro")
    public String registerForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }
    

    @PostMapping("/registro")
    public String registro(@Valid @ModelAttribute Usuario usuario, BindingResult bindingResult, @RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Si hay errores de validación de las anotaciones, vuelve a la página de registro.
            return "registro"; 
        }

        // Se verifica si el ID (nombre de usuario) ya existe
        if (userService.existeId(usuario.getId())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario '" + usuario.getId() + "' ya está registrado. Por favor, elige otro.");
            return REDIRECT_REGISTRO;
        }

		if (!usuario.getPassword().equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
			return REDIRECT_REGISTRO;
		}
        // Verificar si el email ya existe
        if (userService.existeEmail(usuario.getCorreo())) {
            redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
            return REDIRECT_REGISTRO;
        }

        //Encriptar la contraseña
        usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        
        // Se genera y establece el token de confirmación y estado de verificación de email
        String token = UUID.randomUUID().toString();
        usuario.setConfirmationToken(token);
        usuario.setEmailVerified(false); // Establecer explícitamente, aunque sea el valor por defecto

        // TODO: Quizás es pronto para guardarlo puesto que no está confirmado todavía 
        sessionData.setUsuario(usuario);
        
        // Registrar el nuevo usuario
        userService.save(usuario);
        
        // TODO: Enviar email de confirmación con el token
        
        redirectAttributes.addFlashAttribute("success", "Usuario registrado correctamente. Por favor, revisa tu email para confirmar tu cuenta.");
        return REDIRECT_INICIO; // O redirigir a una página que informe sobre la necesidad de confirmar el email
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
    	System.out.println("Logout GET!!!");
        session.invalidate();
        return REDIRECT_LOGIN;
    }
    
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
    	System.out.println("Logout POST!!!");
    	HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
        return REDIRECT_LOGIN;
    }
}
