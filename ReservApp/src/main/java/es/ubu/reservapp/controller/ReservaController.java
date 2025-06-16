package es.ubu.reservapp.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.ReservaRepo;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.EstablecimientoService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la gestión de Reservas.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Controller
@Slf4j
@RequestMapping("/misreservas")
@PreAuthorize("isAuthenticated()")
public class ReservaController {

	/**
	 * Datos de la sesión.
	 */
	private SessionData sessionData;
    private final EstablecimientoService establecimientoService;
    private final ReservaRepo reservaRepo;

    /**
	 * Constructor del controlador que inyecta los servicios necesarios.
	 *
	 * @param sessionData Datos de la sesión.
	 * @param establecimientoService Servicio para gestionar establecimientos.
	 * @param reservaRepo Repositorio de reservas.
	 */
    public ReservaController(SessionData sessionData, EstablecimientoService establecimientoService, ReservaRepo reservaRepo) {
    	this.sessionData = sessionData;
        this.establecimientoService = establecimientoService;
        this.reservaRepo = reservaRepo;
    }

    /**
	 * Muestra el calendario de reservas para un establecimiento específico.
	 *
	 * @param establecimientoId ID del establecimiento.
	 * @param model Modelo para pasar datos a la vista.
	 * @param redirectAttributes Atributos para redirección con mensajes flash.
	 * @return Nombre de la vista a mostrar.
	 */
    @GetMapping("/establecimiento/{id}")
    public String mostrarCalendarioReserva(@PathVariable("id") Integer establecimientoId, Model model, RedirectAttributes redirectAttributes) {
    	Usuario usuario	= sessionData.getUsuario();
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/";
        }

        Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
        if (establecimientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Establecimiento no encontrado.");
            return "redirect:/misreservas"; // O a una página de error general
        }

        Establecimiento establecimiento = establecimientoOpt.get();

        // Verificar si el usuario tiene este establecimiento asignado
        boolean asignado = usuario.getEstablecimiento().stream().anyMatch(e -> e.getId().equals(establecimientoId));
        if (!asignado) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para reservar en este establecimiento.");
            return "redirect:/misreservas";
        }
        
        // Filtrar franjas horarias activas y ordenarlas
        List<FranjaHoraria> franjasActivas = establecimiento.getFranjasHorarias().stream()
            .filter(fh -> establecimiento.isActivo())
            .sorted(Comparator.comparing(FranjaHoraria::getDiaSemana).thenComparing(FranjaHoraria::getHoraInicio))
            .toList();

        model.addAttribute("establecimiento", establecimiento);
        model.addAttribute("franjasHorarias", franjasActivas);
        model.addAttribute("reserva", new Reserva()); // Para el formulario de reserva
        return "reservas/calendario_reserva"; // Ruta a la plantilla Thymeleaf
    }

    /**
	 * Crea una nueva reserva para un establecimiento específico.
	 *
	 * @param reserva Reserva a crear.
	 * @param establecimientoId ID del establecimiento.
	 * @param fechaStr Fecha de la reserva como String (formato ISO).
	 * @param horaStr Hora de la reserva como String (formato ISO).
	 * @param redirectAttributes Atributos para redirección con mensajes flash.
	 * @return Redirección a la página de reservas o error.
	 */
    @PostMapping("/crear")
    public String crearReserva(@ModelAttribute Reserva reserva,
                               @RequestParam("establecimientoId") Integer establecimientoId,
                               @RequestParam("fecha") String fechaStr, // Se recibe como String desde el date picker
                               @RequestParam("hora") String horaStr,   // Se recibe como String desde el time picker
                               RedirectAttributes redirectAttributes) {

    	Usuario usuario	= sessionData.getUsuario();

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no autenticado correctamente.");
            return "redirect:/"; // O a la página de login
        }

        Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
        if (establecimientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Establecimiento no encontrado.");
            return "redirect:/misreservas";
        }
        Establecimiento establecimiento = establecimientoOpt.get();

        // Verificar si el usuario tiene este establecimiento asignado
        boolean asignado = usuario.getEstablecimiento().stream().anyMatch(e -> e.getId().equals(establecimientoId));
        if (!asignado) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para reservar en este establecimiento.");
            return "redirect:/misreservas";
        }

        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(fechaStr);
            hora = LocalTime.parse(horaStr);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Formato de fecha u hora inválido.");
            return "redirect:/reservas/establecimiento/" + establecimientoId;
        }
        
        LocalDateTime fechaReserva = LocalDateTime.of(fecha, hora);

        // Validar franja horaria
        DayOfWeek diaSemanaReserva = fechaReserva.getDayOfWeek();
        boolean dentroDeFranja = false;
        for (FranjaHoraria franja : establecimiento.getFranjasHorarias()) {
            // Asegurarse que la franja pertenezca al día de la semana de la reserva
			if (franja.getDiaSemana() == diaSemanaReserva
					&& !fechaReserva.toLocalTime().isBefore(franja.getHoraInicio())
					&& fechaReserva.toLocalTime().isBefore(franja.getHoraFin())) {
				dentroDeFranja = true;
				break;
			}
		}

        if (!dentroDeFranja) {
            redirectAttributes.addFlashAttribute("error", "La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida.");
            return "redirect:/reservas/establecimiento/" + establecimientoId;
        }

        // TODO: Validación de capacidad (Asegurarse que no se exceda el aforo)
        // if (reservasExistentes >= establecimiento.getAforo()) {
        //    redirectAttributes.addFlashAttribute("error", "Aforo completo para la fecha y hora seleccionada.");
        //    return "redirect:/reservas/establecimiento/" + establecimientoId;
        // }

        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(fechaReserva);

        try {
            reservaRepo.save(reserva);
            redirectAttributes.addFlashAttribute("exito", "Reserva creada correctamente para el " + fechaReserva.toLocalDate() + " a las " + fechaReserva.toLocalTime());
            return "redirect:/misreservas"; // O a una página de confirmación/listado de mis reservas
        } catch (Exception e) {
            // Podría ser una DataIntegrityViolationException si hay constraints, u otra.
            redirectAttributes.addFlashAttribute("error", "Error al guardar la reserva: " + e.getMessage());
            return "redirect:/reservas/establecimiento/" + establecimientoId;
        }
    }
    
    /**
	 * Muestra las reservas del usuario actual.
	 *
	 * @param model Modelo para pasar datos a la vista.
	 * @param redirectAttributes Atributos para redirección con mensajes flash.
	 * @return Nombre de la vista a mostrar.
	 */
    @GetMapping("/misreservas")
    public String mostrarMisReservas(Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario	= sessionData.getUsuario();
            
            if (usuario == null) {
                log.error("Usuario de la sesión no encontrado");
                redirectAttributes.addFlashAttribute("error", "Usuario no autenticado correctamente.");
                model.addAttribute("error", "Usuario no encontrado");
                return "error";
            }

            // Obtener establecimientos activos para mostrar
            List<Establecimiento> establecimientos = establecimientoService.findAll();
            model.addAttribute("establecimientos", establecimientos);
            
            return "reservas/misreservas";

        } catch (Exception e) {
        	log.error("Error al mostrar mis reservas: {}", e.getMessage(), e);
            model.addAttribute("error", "Error interno del servidor");
            return "error";
        }
    }
}
