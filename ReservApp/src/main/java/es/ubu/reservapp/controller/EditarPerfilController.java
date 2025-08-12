package es.ubu.reservapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la edición del perfil del usuario actual.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
@RequestMapping("/perfiles")
@PreAuthorize("isAuthenticated()")
public class EditarPerfilController {

    private static final String USUARIO_ATTRIBUTE = "usuario";
    private static final String PERFIL_VIEW = "perfiles/editar_perfil";
    private static final String REDIRECT_MENU = "redirect:/menuprincipal";
    private static final String ERROR = "error";
    private static final String EXITO = "exito";

    /**
     * Datos de la sesión.
     */
    private final SessionData sessionData;

    /**
     * Servicio para gestionar usuarios.
     */
    private final UsuarioService usuarioService;

    /**
     * Constructor del controlador que inyecta las dependencias.
     *
     * @param sessionData   los datos de la sesión actual.
     * @param usuarioService el servicio para gestionar usuarios.
     */
    public EditarPerfilController(SessionData sessionData, UsuarioService usuarioService) {
        this.sessionData = sessionData;
        this.usuarioService = usuarioService;
    }

    /**
     * Muestra el formulario para editar el perfil del usuario actual.
     *
     * @param model              el modelo para pasar datos a la vista.
     * @param redirectAttributes atributos para redirección con mensajes flash.
     * @return el nombre de la vista del formulario o redirección si hay error.
     */
    @GetMapping
    public String mostrarFormularioEditarPerfil(Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioActual = sessionData.getUsuario();
            if (usuarioActual == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Usuario no autenticado correctamente.");
                return REDIRECT_MENU;
            }

            // Crear una copia del usuario para editar (sin la contraseña)
            Usuario usuarioParaEditar = new Usuario();
            usuarioParaEditar.setId(usuarioActual.getId());
            usuarioParaEditar.setNombre(usuarioActual.getNombre());
            usuarioParaEditar.setApellidos(usuarioActual.getApellidos());
            usuarioParaEditar.setCorreo(usuarioActual.getCorreo());
            usuarioParaEditar.setTelefono(usuarioActual.getTelefono());
            usuarioParaEditar.setAdministrador(usuarioActual.isAdministrador());
            usuarioParaEditar.setBloqueado(usuarioActual.isBloqueado());
            usuarioParaEditar.setPassword(null); // No mostrar la contraseña

            model.addAttribute(USUARIO_ATTRIBUTE, usuarioParaEditar);
            ControllerHelper.setEditMode(model, true);
            
            return PERFIL_VIEW;
        } catch (Exception e) {
            log.error("Error al cargar el perfil del usuario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR, "Error al cargar el perfil: " + e.getMessage());
            return REDIRECT_MENU;
        }
    }

    /**
     * Guarda los cambios del perfil del usuario actual.
     *
     * @param usuario            el objeto Usuario con los datos actualizados.
     * @param bindingResult      resultados de la validación del usuario.
     * @param redirectAttributes atributos para redirección con mensajes flash.
     * @param model              el modelo para pasar datos a la vista en caso de error.
     * @return redirección al menú principal o vista del formulario en caso de error.
     */
    @PostMapping("/guardar")
    public String guardarPerfil(@Valid @ModelAttribute(USUARIO_ATTRIBUTE) Usuario usuario,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            Usuario usuarioActual = sessionData.getUsuario();
            if (usuarioActual == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Usuario no autenticado correctamente.");
                return REDIRECT_MENU;
            }

            // Verificar que el usuario está editando su propio perfil
            if (!usuarioActual.getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ERROR, "No tienes permisos para editar este perfil.");
                return REDIRECT_MENU;
            }

            if (bindingResult.hasErrors()) {
                ControllerHelper.setEditMode(model, true);
                return PERFIL_VIEW;
            }

            // Validar email único (excluyendo el usuario actual)
            if (!usuarioActual.getCorreo().equalsIgnoreCase(usuario.getCorreo()) && usuarioService.existeEmail(usuario.getCorreo())) {
                bindingResult.rejectValue("correo", "error.usuario", "Ya existe un usuario con este correo electrónico.");
                ControllerHelper.setEditMode(model, true);
                return PERFIL_VIEW;
            }

            // Actualizar solo los campos permitidos
            usuarioActual.setNombre(usuario.getNombre());
            usuarioActual.setApellidos(usuario.getApellidos());
            usuarioActual.setCorreo(usuario.getCorreo().toLowerCase());
            usuarioActual.setTelefono(usuario.getTelefono());

            // Solo actualizar contraseña si se proporcionó una nueva
            if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
                usuarioActual.setPassword(usuario.getPassword());
            }

            usuarioService.save(usuarioActual);
            
            // Actualizar el usuario en la sesión
            sessionData.setUsuario(usuarioActual);
            
            redirectAttributes.addFlashAttribute(EXITO, "Perfil actualizado correctamente.");
            return REDIRECT_MENU;

        } catch (Exception e) {
            log.error("Error al guardar el perfil: {}", e.getMessage(), e);
            model.addAttribute(ERROR, "Error al guardar el perfil: " + e.getMessage());
            ControllerHelper.setEditMode(model, true);
            return PERFIL_VIEW;
        }
    }
}