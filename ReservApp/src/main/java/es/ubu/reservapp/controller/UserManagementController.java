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
    	
        model.addAttribute("users", users);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("blockedCount", blockedCount);
        return "admin/user_management";
    }

    @GetMapping("/usuarios/nuevo")
    public String showCreateUserForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("isEdit", false);
        return ADMIN_FORM;
    }

    @GetMapping("/usuarios/editar/{id}")
    public String showEditUserForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.findUsuarioById(id);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return ADMIN_USUARIOS;
        }
        usuario.setPassword(null); 
        model.addAttribute("usuario", usuario);
        model.addAttribute("isEdit", true);
        return ADMIN_FORM;
    }

    @PostMapping("/usuarios/guardar")
    public String saveOrUpdateUser(@Valid @ModelAttribute("usuario") Usuario usuario, 
                                 BindingResult bindingResult, Model model, 
                                 RedirectAttributes redirectAttributes) {
        
        boolean isNewUser = usuario.getId() == null || usuario.getId().trim().isEmpty() || !preExistingId(usuario.getId());

        if (bindingResult.hasErrors() && !(isNewUser && usuario.getId() == null && bindingResult.getErrorCount() == 1 && bindingResult.hasFieldErrors("id"))) {
            if (!isNewUser && bindingResult.hasFieldErrors("id") && usuario.getId().isEmpty()){
			} else if (isNewUser && usuario.getId() == null && bindingResult.getErrorCount() == 1 && bindingResult.getFieldErrorCount("id") == 1) {
			} else if (bindingResult.hasErrors()) {
				model.addAttribute("isEdit", !isNewUser);
				return ADMIN_FORM;
			}
		}

        if (isNewUser) {
            if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
                 bindingResult.rejectValue("id", "error.usuario", "El ID de usuario es obligatorio.");
            } else if (usuarioService.existeId(usuario.getId())) {
                bindingResult.rejectValue("id", "error.usuario", "El nombre de usuario ya está registrado.");
            }
            Usuario existingByEmail = usuarioService.findUsuarioByCorreo(usuario.getCorreo());
            if (existingByEmail != null) {
                 bindingResult.rejectValue("correo", "error.usuario", "El email ya está registrado.");
            }
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                bindingResult.rejectValue("password", "error.usuario", "La contraseña es obligatoria para nuevos usuarios.");
            }
        } else { 
            Usuario existingUserByEmail = usuarioService.findUsuarioByCorreo(usuario.getCorreo());
            if(existingUserByEmail != null && !existingUserByEmail.getId().equals(usuario.getId())) {
                bindingResult.rejectValue("correo", "error.usuario", "El email ya está registrado por otro usuario.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", !isNewUser);
            return ADMIN_FORM;
        }

        if (isNewUser) {
            usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
        } else { 
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                Usuario existingUser = usuarioService.findUsuarioById(usuario.getId());
                if (existingUser != null) {
                    usuario.setPassword(existingUser.getPassword()); 
                }
            } else {
                usuario.setPassword(new BCryptPasswordEncoder().encode(usuario.getPassword()));
            }
        }
        
        if (usuario.getCorreo() != null) {
            usuario.setCorreo(usuario.getCorreo().toLowerCase());
        }

        usuarioService.save(usuario);
        redirectAttributes.addFlashAttribute("exito", "Usuario " + (isNewUser ? "creado" : "actualizado") + " correctamente.");
        return ADMIN_USUARIOS;
    }
    
    private boolean preExistingId(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return usuarioService.existeId(id); 
    }

    @PostMapping("/usuarios/bloquear/{id}")
    public String blockUser(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
			usuarioService.blockUser(id);
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        redirectAttributes.addFlashAttribute("exito", "Usuario bloqueado correctamente.");
        return ADMIN_USUARIOS;
    }

    @PostMapping("/usuarios/desbloquear/{id}")
    public String unblockUser(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
			usuarioService.unblockUser(id);
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        redirectAttributes.addFlashAttribute("exito", "Usuario desbloqueado correctamente.");
        return ADMIN_USUARIOS;
    }
}
