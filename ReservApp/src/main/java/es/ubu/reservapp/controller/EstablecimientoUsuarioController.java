package es.ubu.reservapp.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para gestionar la visualización de establecimientos asignados a
 * usuarios regulares.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
@RequestMapping("/establecimientos")
@PreAuthorize("isAuthenticated()")
public class EstablecimientoUsuarioController {

    /**
     * Datos de la sesión.
     */
    private SessionData sessionData;

    /**
     * Constructor del controlador que inyecta los datos de la sesión.
     * 
     * @param sessionData Datos de la sesión.
     */
    public EstablecimientoUsuarioController(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * Muestra los establecimientos asignados al usuario para realizar reservas.
     * 
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para redirecciones.
     * @return La vista de establecimientos del usuario o redirección en caso de error.
     */
    @GetMapping({"", "/", "/listado"})
    public String listarEstablecimientosUsuario(Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = sessionData.getUsuario();
            
            if (usuario == null) {
                log.error("Usuario de la sesión no encontrado");
                redirectAttributes.addFlashAttribute("error", "Usuario no autenticado correctamente.");
                return "redirect:/";
            }

            List<Establecimiento> establecimientos = usuario.getLstEstablecimientos();
            model.addAttribute("establecimientos", establecimientos);
            
            return "establecimientos/listado_usuario";

        } catch (Exception e) {
            log.error("Error al mostrar establecimientos del usuario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error interno del servidor");
            return "redirect:/menuprincipal";
        }
    }
}