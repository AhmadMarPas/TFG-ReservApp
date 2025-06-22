package es.ubu.reservapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Perfil;
import es.ubu.reservapp.service.PerfilService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la gestión de Perfiles por parte de administradores.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
@RequestMapping("/admin/perfiles")
@PreAuthorize("hasAuthority('ADMIN')")
public class PerfilController {

    private static final String PERFIL_ATTRIBUTE = "perfil";
    private static final String LISTADO_VIEW = "perfiles/listado";
    private static final String FORMULARIO_VIEW = "perfiles/formulario";
    private static final String REDIRECT_LISTADO = "redirect:/admin/perfiles/listado";

	/**
	 * Servicio para gestionar perfiles.
	 */
    private final PerfilService perfilService;

	/**
	 * Constructor del controlador que inyecta el servicio de perfiles.
	 *
	 * @param perfilService el servicio para gestionar perfiles.
	 */
    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

	/**
	 * Muestra el listado de todos los perfiles.
	 *
	 * @param model el modelo para pasar datos a la vista.
	 * @return el nombre de la vista para mostrar el listado.
	 */
    //@GetMapping
    @GetMapping({"", "/", "/listado"})
    public String listarPerfiles(Model model) {
        try {
            List<Perfil> perfiles = perfilService.findAll();
            long perfilActivoCount = perfiles.stream().count();
            model.addAttribute("perfiles", perfiles);
            model.addAttribute("perfilActivoCount", perfilActivoCount);
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la lista de perfiles: " + e.getMessage());
        }
        return LISTADO_VIEW;
    }

	/**
	 * Muestra el formulario para crear un nuevo perfil.
	 *
	 * @param model el modelo para pasar datos a la vista.
	 * @return el nombre de la vista del formulario.
	 */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoPerfil(Model model) {
        model.addAttribute(PERFIL_ATTRIBUTE, new Perfil());
        model.addAttribute("isEdit", false);
        return FORMULARIO_VIEW;
    }

	/**
	 * Muestra el formulario para editar un perfil existente.
	 *
	 * @param id                 el ID del perfil a editar.
	 * @param model              el modelo para pasar datos a la vista.
	 * @param redirectAttributes atributos para redirección con mensajes flash.
	 * @return el nombre de la vista del formulario o redirección al listado si no
	 *         se encuentra el perfil.
	 */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarPerfil(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
        	Optional<Perfil> perfilOptional = perfilService.findById(id);
        	if (!perfilOptional.isPresent()) { 
                redirectAttributes.addFlashAttribute("error", "Perfil no encontrado");
                return REDIRECT_LISTADO;
            }
            model.addAttribute("perfil", perfilOptional.get());
            model.addAttribute("isEdit", true);
            return FORMULARIO_VIEW;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el perfil: " + e.getMessage());
            return REDIRECT_LISTADO;
        }
    }

	/**
	 * Guarda un perfil nuevo o actualizado.
	 *
	 * @param perfil             el objeto Perfil a guardar.
	 * @param bindingResult      resultados de la validación del perfil.
	 * @param redirectAttributes atributos para redirección con mensajes flash.
	 * @param model              el modelo para pasar datos a la vista en caso de
	 *                           error.
	 * @return redirección al listado de perfiles o vista del formulario en caso de
	 *         error.
	 */
    @PostMapping("/guardar")
    public String guardarPerfil(@Valid @ModelAttribute(PERFIL_ATTRIBUTE) Perfil perfil,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
        	model.addAttribute("isEdit", perfil.getId() != null);
            return FORMULARIO_VIEW;
        }

        try {
            // Determinar si es creación o actualización
			boolean isUpdate = perfil.getId() != null;

			if (isUpdate) {
				// Actualizar perfil existente
				Optional<Perfil> perfilOptional = perfilService.findById(perfil.getId());
				if (!perfilOptional.isPresent()) {
					redirectAttributes.addFlashAttribute("error", "Perfil no encontrado para actualizar");
					return REDIRECT_LISTADO;
				} else if ((perfil.getId() == null || !perfilOptional.get().getId().equals(perfil.getId()))) {
					bindingResult.rejectValue("nombre", "error.perfil", "Ya existe un perfil con este nombre.");
					model.addAttribute(PERFIL_ATTRIBUTE, perfil);
					return FORMULARIO_VIEW;
				}

				perfilService.save(perfil);
				redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente");
			} else {
				// Crear nuevo perfil
				perfilService.save(perfil);
				redirectAttributes.addFlashAttribute("exito", "Perfil creado correctamente");
			}

			return REDIRECT_LISTADO;

		} catch (Exception e) {
			model.addAttribute("error", "Error al guardar el perfil: " + e.getMessage());
			model.addAttribute("isEdit", perfil.getId() != null);
			return FORMULARIO_VIEW;
		}
    }

	/**
	 * Elimina un perfil por su ID.
	 *
	 * @param id                 el ID del perfil a eliminar.
	 * @param redirectAttributes atributos para redirección con mensajes flash.
	 * @return redirección al listado de perfiles tras la eliminación.
	 */
    @PostMapping("/eliminar/{id}")
    public String eliminarPerfil(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        // Validar si el perfil "ADMIN" o algún otro perfil crítico se está intentando eliminar
        Optional<Perfil> perfilOptional = perfilService.findById(id);
        if (perfilOptional.isPresent()) {
            Perfil perfil = perfilOptional.get();
            if ("ADMIN".equalsIgnoreCase(perfil.getNombre())) {
                redirectAttributes.addFlashAttribute("error", "El perfil 'ADMIN' no puede ser eliminado.");
                return REDIRECT_LISTADO;
            }
            // Aquí también se podría verificar si el perfil está en uso por algún usuario.
            // Si está en uso, se podría impedir la eliminación o mostrar una advertencia.
        } else {
            redirectAttributes.addFlashAttribute("error", "Perfil no encontrado con ID: " + id);
            return REDIRECT_LISTADO;
        }

        try {
            perfilService.deleteById(id);
            redirectAttributes.addFlashAttribute("exito", "Perfil eliminado correctamente.");
        } catch (Exception e) {
            log.error("Error al eliminar perfil: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el perfil. Es posible que esté asignado a usuarios.");
        }
        return REDIRECT_LISTADO;
    }
}
