package es.ubu.reservapp.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la asignación de establecimientos a usuarios.
 * 
 * @author Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
public class EstablecimientoAsignacionController {

	private static final String REDIRECT = "redirect:/";
	private static final String REDIRECT_ADMIN_USUARIOS = REDIRECT + "admin/usuarios";
	private static final String REDIRECT_MENUPRINCIPAL = REDIRECT + "menuprincipal";
	private static final String ASIGNACION = "establecimientos/asignacion";
	private static final String ADMIN = "admin";
	private static final String USUARIO = "usuario";
	private static final String ERROR = "error";
	private static final String EXITO = "exito";
	private static final String REDIRECT_ERROR = REDIRECT + ERROR;

    /**
     * Datos de la sesión.
     */
    private SessionData sessionData;

    /**
	 * Servicio de establecimientos
	 */
    private final EstablecimientoService establecimientoService;
    
	/**
	 * Servicio de Usuario
	 */
    private final UsuarioService usuarioService;

    /**
     * Constructor del controlador.
     * 
     * @param establecimientoService Servicio de establecimientos
     * @param usuarioService Servicio de usuarios
     * @param sessionData Datos de sesión
     */
    public EstablecimientoAsignacionController(SessionData sessionData, EstablecimientoService establecimientoService, UsuarioService usuarioService) {
    	this.sessionData = sessionData;
        this.establecimientoService = establecimientoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Muestra la página de asignación de establecimientos para el usuario actual.
     * 
     * @param model Modelo de la vista
     * @return Vista de asignación de establecimientos
     */
    @GetMapping("/establecimientos/asignar")
    public String mostrarAsignacionEstablecimientos(Model model) {
        Usuario usuarioActual = sessionData.getUsuario();
        return mostrarAsignacionEstablecimientosParaUsuario(usuarioActual.getId(), model, USUARIO);
    }

    /**
     * Muestra la página de asignación de establecimientos para un usuario específico (admin).
     * 
     * @param userId ID del usuario
     * @param model Modelo de la vista
     * @return Vista de asignación de establecimientos
     */
    @GetMapping("/admin/usuarios/{userId}/establecimientos")
    public String mostrarAsignacionEstablecimientosAdmin(@PathVariable String userId, Model model) {
        return mostrarAsignacionEstablecimientosParaUsuario(userId, model, ADMIN);
    }

    /**
     * Método común para mostrar la asignación de establecimientos.
     * 
     * @param userId ID del usuario
     * @param model Modelo de la vista
     * @param origen Origen de la petición ("usuario" o "admin")
     * @return Vista de asignación de establecimientos
     */
    private String mostrarAsignacionEstablecimientosParaUsuario(String userId, Model model, String origen) {
        Usuario usuario = usuarioService.findUsuarioById(userId);
        if (usuario == null) {
            log.error("Usuario no encontrado: {}", userId);
            return REDIRECT_ERROR;
        }

        List<Establecimiento> todosEstablecimientos = establecimientoService.findAll();
        Set<Integer> establecimientosAsignados = usuario.getLstEstablecimientos().stream()
                .map(Establecimiento::getId)
                .collect(Collectors.toSet());

        model.addAttribute(USUARIO, usuario);
        model.addAttribute("establecimientos", todosEstablecimientos);
        model.addAttribute("establecimientosAsignados", establecimientosAsignados);
        model.addAttribute("origen", origen);

        return ASIGNACION;
    }

    /**
     * Guarda la asignación de establecimientos para el usuario actual.
     * 
     * @param establecimientosIds IDs de los establecimientos seleccionados
     * @param redirectAttributes Atributos de redirección
     * @return Redirección
     */
    @PostMapping("/establecimientos/asignar")
    public String guardarAsignacionEstablecimientos(
            @RequestParam(value = "establecimientosIds", required = false) List<Integer> establecimientosIds,
            RedirectAttributes redirectAttributes) {
        Usuario usuarioActual = sessionData.getUsuario();
        return guardarAsignacionEstablecimientosParaUsuario(usuarioActual.getId(), establecimientosIds, redirectAttributes, USUARIO);
    }

    /**
     * Guarda la asignación de establecimientos para un usuario específico (admin).
     * 
     * @param userId ID del usuario
     * @param establecimientosIds IDs de los establecimientos seleccionados
     * @param redirectAttributes Atributos de redirección
     * @return Redirección
     */
    @PostMapping("/admin/usuarios/{userId}/establecimientos")
    public String guardarAsignacionEstablecimientosAdmin(@PathVariable String userId,
            @RequestParam(value = "establecimientosIds", required = false) List<Integer> establecimientosIds,
            RedirectAttributes redirectAttributes) {
        return guardarAsignacionEstablecimientosParaUsuario(userId, establecimientosIds, redirectAttributes, ADMIN);
    }

    /**
     * Método común para guardar la asignación de establecimientos.
     * 
     * @param userId ID del usuario
     * @param establecimientosIds IDs de los establecimientos seleccionados
     * @param redirectAttributes Atributos de redirección
     * @param origen Origen de la petición
     * @return Redirección
     */
    private String guardarAsignacionEstablecimientosParaUsuario(String userId, List<Integer> establecimientosIds,
            RedirectAttributes redirectAttributes, String origen) {
        try {
            Usuario usuario = usuarioService.findUsuarioById(userId);
            if (usuario == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Usuario no encontrado.");
                return ADMIN.equals(origen) ? REDIRECT_ADMIN_USUARIOS : REDIRECT_MENUPRINCIPAL;
            }

            // Limpiar establecimientos actuales
            usuario.getLstEstablecimientos().clear();

            // Agregar nuevos establecimientos si se seleccionaron
            if (establecimientosIds != null && !establecimientosIds.isEmpty()) {
//            	establecimientoService.findAllById(establecimientosIds).forEach(establecimiento -> usuario.getLstEstablecimientos().add(establecimiento));
                for (Integer establecimientoId : establecimientosIds) {
                    establecimientoService.findById(establecimientoId).ifPresent(establecimiento -> usuario.getLstEstablecimientos().add(establecimiento));
                }
            }

            usuarioService.save(usuario);
            redirectAttributes.addFlashAttribute(EXITO, "Establecimientos asignados correctamente.");

            if (ADMIN.equals(origen)) {
                return REDIRECT_ADMIN_USUARIOS;
            } else {
                return REDIRECT_MENUPRINCIPAL;
            }

        } catch (Exception e) {
            log.error("Error al asignar establecimientos al usuario {}: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute(ERROR, "Error al asignar establecimientos.");
            return ADMIN.equals(origen) ? REDIRECT_ADMIN_USUARIOS : REDIRECT_MENUPRINCIPAL;
        }
    }
}