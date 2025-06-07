package es.ubu.reservapp.controller;

import java.util.List;
import java.util.ArrayList; 
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

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.service.EstablecimientoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j; 

/**
 * Controlador para la gestión de Establecimientos por parte de administradores.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
@RequestMapping("/admin/establecimientos")
@PreAuthorize("hasAuthority('ADMIN')") 
public class EstablecimientoController {

	private static final String ESTABLECIMIENTO = "establecimiento";
	private static final String ESTABLECIMIENTO_LISTADO = "establecimientos/listado";
	private static final String REDIRECT_LISTADO = "redirect:/admin/" + ESTABLECIMIENTO_LISTADO;
	private static final String REDIRECT_FORMULARIO = "establecimientos/formulario";
	
	/** 
	 * Servicio para gestionar establecimientos.
	 */
    private final EstablecimientoService establecimientoService;

	/**
	 * Constructor del controlador que inyecta el servicio de establecimientos.
	 * 
	 * @param establecimientoService el servicio para gestionar establecimientos.
	 */
    public EstablecimientoController(EstablecimientoService establecimientoService) {
        this.establecimientoService = establecimientoService;
    }

    /**
     * Muestra el listado de todos los establecimientos.
     * 
     * @param model el modelo para pasar datos a la vista.
     * @return el nombre de la vista para mostrar el listado.
     */
    @GetMapping({"", "/", "/listado"}) 
    public String listarEstablecimientos(Model model) {
        List<Establecimiento> establecimientos = establecimientoService.findAll();
        model.addAttribute("establecimientos", establecimientos);
        return "establecimientos/listado"; 
    }

    /**
     * Muestra el formulario para crear un nuevo establecimiento.
     * 
     * @param model el modelo para pasar datos a la vista.
     * @return el nombre de la vista del formulario.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoEstablecimiento(Model model) {
        model.addAttribute(ESTABLECIMIENTO, new Establecimiento());
        return REDIRECT_FORMULARIO;
    }

    /**
     * Muestra el formulario para editar un establecimiento existente.
     * 
     * @param id el ID del establecimiento a editar.
     * @param model el modelo para pasar datos a la vista.
     * @param redirectAttributes atributos para pasar mensajes tras una redirección.
     * @return el nombre de la vista del formulario o una redirección si no se encuentra.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarEstablecimiento(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Establecimiento> establecimientoOptional = establecimientoService.findById(id);

        if (establecimientoOptional.isPresent()) {
            model.addAttribute(ESTABLECIMIENTO, establecimientoOptional.get());
            return REDIRECT_FORMULARIO;
        } else {
            redirectAttributes.addFlashAttribute("error", "Establecimiento no encontrado con ID: " + id);
            return REDIRECT_LISTADO;
        }
    }

    /**
     * Guarda un nuevo establecimiento o actualiza uno existente.
     * 
     * @param establecimiento el establecimiento a guardar, validado.
     * @param bindingResult el resultado de la validación.
     * @param redirectAttributes atributos para pasar mensajes tras una redirección.
     * @param model el modelo para pasar datos a la vista en caso de error sin redirección.
     * @return el nombre de la vista a la que redirigir o mostrar.
     */
    @PostMapping("/guardar")
    public String guardarEstablecimiento(@Valid @ModelAttribute("establecimiento") Establecimiento establecimiento,
                                         BindingResult bindingResult,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        if (bindingResult.hasErrors()) {
            return REDIRECT_FORMULARIO;
        }

        try {
            if (establecimiento.getId() == null && establecimiento.getFranjasHorarias() == null) {
                establecimiento.setFranjasHorarias(new ArrayList<>());
            }
            
            establecimientoService.save(establecimiento);
            redirectAttributes.addFlashAttribute("exito", "Establecimiento guardado correctamente.");
            return REDIRECT_LISTADO;
        } catch (Exception e) {
            log.error("Error al guardar establecimiento: {}", e.getMessage(), e); 
            model.addAttribute(ESTABLECIMIENTO, establecimiento); 
            model.addAttribute("error", "Error al guardar el establecimiento: " + e.getMessage());
            return REDIRECT_FORMULARIO;
        }
    }
}
