package es.ubu.reservapp.controller;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/admin")
public class UserManagementController {
	
	private static final String ADMIN_USUARIOS = "redirect:/admin/usuarios";
	private static final String ADMIN_FORM = "admin/user_form";
	private static final String ERROR = "error";
	private static final String ERROR_USUARIO = "error.usuario";
	private static final String EXITO = "exito";

	/** 
	 * Servicio de usuario para gestionar operaciones relacionadas con usuarios.
	 */
    private final UsuarioService usuarioService;

	/**
	 * Constructor de UserManagementController.
	 *
	 * @param usuarioService  Servicio de usuario
	 */
    public UserManagementController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public String listUsers(Model model) {
    	List<Usuario> users = usuarioService.findAll();
    	long adminCount = users.stream().filter(Usuario::isAdministrador).count();
    	long blockedCount = users.stream().filter(Usuario::isBloqueado).count();
    	
        model.addAttribute("usuarios", users);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("blockedCount", blockedCount);
        return "admin/user_management";
    }

    @GetMapping("/usuarios/nuevo")
    public String showCreateUserForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        ControllerHelper.setEditMode(model, false);
        return ADMIN_FORM;
    }

    @GetMapping("/usuarios/editar/{id}")
	public String showEditUserForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
		Usuario usuario = usuarioService.findUsuarioById(id);
		if (usuario == null) {
			redirectAttributes.addFlashAttribute(ERROR, "Usuario no encontrado.");
			return ADMIN_USUARIOS;
		}
		usuario.setPassword(null);
		model.addAttribute("usuario", usuario);
		ControllerHelper.setEditMode(model, true);
		return ADMIN_FORM;
	}

	@PostMapping("/usuarios/guardar")
	public String saveOrUpdateUser(@Valid @ModelAttribute("usuario") Usuario usuario,
	        BindingResult bindingResult, @RequestParam(name = "editMode", defaultValue = "false") boolean isEdit,
	        Model model, RedirectAttributes redirectAttributes) {

	    boolean isNewUser = !isEdit;

	    // Si es nuevo usuario y el ID ya existe, no permitir la creación
	    if (isNewUser && usuarioService.existeId(usuario.getId())) {
	        bindingResult.rejectValue("id", ERROR_USUARIO, "No se puede crear un nuevo usuario. El ID ya existe en el sistema.");
	        ControllerHelper.setEditMode(model, false);
	        return ADMIN_FORM;
	    }

	    // Si es modificación y el ID no existe, error
	    if (!isNewUser && !usuarioService.existeId(usuario.getId())) {
	        bindingResult.rejectValue("id", ERROR_USUARIO, "No se puede modificar el usuario. El ID no existe en el sistema.");
	        ControllerHelper.setEditMode(model, true);
	        return ADMIN_FORM;
	    }

	    if (!isValidBindingResult(bindingResult, isNewUser)) {
	        ControllerHelper.setEditMode(model, !isNewUser);
	        return ADMIN_FORM;
	    }

	    if (isNewUser && !validateNewUser(usuario, bindingResult)) {
	        ControllerHelper.setEditMode(model, false);
	        return ADMIN_FORM;
	    }

	    if (!validateEmail(usuario, isNewUser, bindingResult)) {
	        ControllerHelper.setEditMode(model, !isNewUser);
	        return ADMIN_FORM;
	    }

	    processPassword(usuario, isNewUser);
	    usuario.setCorreo(usuario.getCorreo().toLowerCase());

	    usuarioService.save(usuario);
	    redirectAttributes.addFlashAttribute(EXITO, "Usuario " + (isNewUser ? "creado" : "actualizado") + " correctamente.");
	    return ADMIN_USUARIOS;
	}

	/**
	 * Valida el resultado del binding para determinar si hay errores.
	 * 
	 * @param bindingResult Resultado del binding
	 * @param isNewUser Indica si es un nuevo usuario
	 * @return true si no hay errores, false en caso contrario
	 */
	private boolean isValidBindingResult(BindingResult bindingResult, boolean isNewUser) {
	    if (!bindingResult.hasErrors()) {
	        return true;
	    }
	    
		if (isNewUser && bindingResult.getErrorCount() == 1 && bindingResult.hasFieldErrors("id")
				&& bindingResult.getFieldError("id").getRejectedValue() == null) {
			return true;
		}

		return !isNewUser && bindingResult.hasFieldErrors("id")
				&& bindingResult.getFieldError("id").getRejectedValue().toString().isEmpty();
	}

	/**
	 * Valida un nuevo usuario.
	 * 
	 * @param usuario Usuario a validar
	 * @param bindingResult Resultado del binding
	 * @return true si el usuario es válido, false en caso contrario
	 */
	private boolean validateNewUser(Usuario usuario, BindingResult bindingResult) {
	    // Ahora solo validamos la contraseña ya que el ID se valida en validateUserId
	    if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
	        bindingResult.rejectValue("password", ERROR_USUARIO, "La contraseña es obligatoria para nuevos usuarios.");
	        return false;
	    }
	    return true;
	}

	/**	
	 * Valida el email del usuario.
	 * 
	 * @param usuario Usuario a validar
	 * @param isNewUser Indica si es un nuevo usuario
	 * @param bindingResult Resultado del binding
	 * @return true si el email es válido, false en caso contrario
	 */
	private boolean validateEmail(Usuario usuario, boolean isNewUser, BindingResult bindingResult) {
	    Usuario existingByEmail = usuarioService.findUsuarioByCorreo(usuario.getCorreo());
	    
	    if (isNewUser && existingByEmail != null) {
	        bindingResult.rejectValue("correo", ERROR_USUARIO, "El email ya está registrado.");
	        return false;
	    }
	    
	    if (!isNewUser && existingByEmail != null && !existingByEmail.getId().equals(usuario.getId())) {
	        bindingResult.rejectValue("correo", ERROR_USUARIO, "El email ya está registrado por otro usuario.");
	        return false;
	    }
	    
	    return true;
	}

	/**
	 * Procesa la contraseña del usuario. Si es un nuevo usuario, se codifica la
	 * contraseña. Si es una modificación y no se proporciona una nueva contraseña,
	 * se mantiene la existente.
	 * 
	 * @param usuario   Usuario a procesar
	 * @param isNewUser Indica si es un nuevo usuario
	 */
	private void processPassword(Usuario usuario, boolean isNewUser) {
	    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	    
	    if (isNewUser || (usuario.getPassword() != null && !usuario.getPassword().isEmpty())) {
	        usuario.setPassword(encoder.encode(usuario.getPassword()));
	    } else {
	        Usuario existingUser = usuarioService.findUsuarioById(usuario.getId());
	        if (existingUser != null) {
	            usuario.setPassword(existingUser.getPassword());
	        }
	    }
	}

    @PostMapping("/usuarios/bloquear/{id}")
    public String blockUser(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
			usuarioService.blockUser(id);
		} catch (UserNotFoundException e) {
            log.error("Error al bloquear el usuario con ID: " + id, e);
            redirectAttributes.addFlashAttribute(ERROR, "Usuario no encontrado o ya bloqueado.");
            return ADMIN_USUARIOS;
        }
        redirectAttributes.addFlashAttribute(EXITO, "Usuario bloqueado correctamente.");
        return ADMIN_USUARIOS;
    }

    @PostMapping("/usuarios/desbloquear/{id}")
    public String unblockUser(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
			usuarioService.unblockUser(id);
		} catch (UserNotFoundException e) {
			log.error("Error al desbloquear el usuario con ID: " + id, e);
			redirectAttributes.addFlashAttribute(ERROR, "Usuario no encontrado o ya desbloqueado.");
			return ADMIN_USUARIOS;
		}
        redirectAttributes.addFlashAttribute(EXITO, "Usuario desbloqueado correctamente.");
        return ADMIN_USUARIOS;
    }
}
