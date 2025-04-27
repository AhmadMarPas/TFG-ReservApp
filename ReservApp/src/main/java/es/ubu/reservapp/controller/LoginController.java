package es.ubu.reservapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

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
	private static final String REDIRECT_INICIO = "redirect:/inicio";

	private UsuarioService userService;

	/**
	 * Datos de la sesión.
	 */
	private SessionData sessionData;

	/**
	 * Constructor sin parametros.
	 */
//	public LoginController() {
//		sessionData = new SessionData();
//	}
	
	/**
	 * Constructor de LoginController.
	 * 
	 * @param sessionData Datos de la sesión
	 */
	public LoginController(UsuarioService userService, SessionData sessionData) {
		this.userService = userService;
		this.sessionData = sessionData;
	}
	
	/**
	 * Constructor de LoginController.
	 * 
	 * @param userService Servicio de usuario
	 */
//	@Autowired
//	public LoginController(UsuarioService userService) {
//        this.userService = userService;
////        sessionData = new SessionData();
//    }

	@GetMapping("/loginer")
    public String loginForm(Model model) {
		model.addAttribute("usuario", new Usuario());
        return "login";
    }
	
	@PostMapping("/loginer")
//	@RequestMapping(value = "/login", method = RequestMethod.POST)
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
			return "menuprincipal";
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
            // Si hay errores de validación, vuelve a la página de registro
            return "registro";
        }
		if (usuario == null) {
			redirectAttributes.addFlashAttribute("error", "El Usuario no puede estar vacío");
			return REDIRECT_REGISTRO;
		}
		if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "La contraseña no puede estar vacía");
			return REDIRECT_REGISTRO;
		}
		if (usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "El nombre no puede estar vacío");
			return REDIRECT_REGISTRO;
		}
		if (usuario.getApellidos() == null || usuario.getApellidos().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Los apellidos no pueden estar vacíos");
			return REDIRECT_REGISTRO;
		}
		if (usuario.getCorreo() == null || usuario.getCorreo().isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "El correo no puede estar vacío");
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
//        usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        usuario.setId(usuario.getNombre().toLowerCase());
        sessionData.setUsuario(usuario);
        // Registrar el nuevo usuario
        userService.save(usuario);
        redirectAttributes.addFlashAttribute("success", "Usuario registrado correctamente. Por favor inicia sesión.");
        return REDIRECT_INICIO;
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
