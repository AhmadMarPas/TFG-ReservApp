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

	private static final String REDIRECT = "redirect:/";
	private static final String REDIRECT_CALENDARIO = "reservas/calendario_reserva";
	private static final String REDIRECT_MIS_RESERVAS = REDIRECT + "misreservas";
	private static final String REDIRECT_RESERVAS_ESTABLECIMIENTO = REDIRECT + "reservas/establecimiento/";
	private static final String ERROR = "error";
	private static final String EXITO = "exito";
	
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
            redirectAttributes.addFlashAttribute(ERROR, "Usuario no encontrado.");
            return REDIRECT;
        }

        Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
        if (establecimientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR, "Establecimiento no encontrado.");
            return REDIRECT_MIS_RESERVAS; // O a una página de error general
        }

        Establecimiento establecimiento = establecimientoOpt.get();

        // Verificar si el usuario tiene este establecimiento asignado
        boolean asignado = usuario.getLstEstablecimientos().stream().anyMatch(e -> e.getId().equals(establecimientoId));
        if (!asignado) {
            redirectAttributes.addFlashAttribute(ERROR, "No tiene permiso para reservar en este establecimiento.");
            return REDIRECT_MIS_RESERVAS;
        }
        
        // Filtrar franjas horarias activas y ordenarlas
        List<FranjaHoraria> franjasActivas = establecimiento.getFranjasHorarias().stream()
            .filter(fh -> establecimiento.isActivo())
            .sorted(Comparator.comparing(FranjaHoraria::getDiaSemana).thenComparing(FranjaHoraria::getHoraInicio))
            .toList();

        // Obtener reservas pasadas y futuras del usuario para este establecimiento
        LocalDateTime fechaActual = LocalDateTime.now();
        List<Reserva> reservasPasadas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
        List<Reserva> reservasFuturas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
        
        // Ordenar las reservas por fecha (más recientes primero para las pasadas, más próximas primero para las futuras)
        reservasPasadas.sort(Comparator.comparing(Reserva::getFechaReserva).reversed());
        reservasFuturas.sort(Comparator.comparing(Reserva::getFechaReserva));

        model.addAttribute("establecimiento", establecimiento);
        model.addAttribute("franjasHorarias", franjasActivas);
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("reservasPasadas", reservasPasadas);
        model.addAttribute("reservasFuturas", reservasFuturas);
        return REDIRECT_CALENDARIO;
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
            redirectAttributes.addFlashAttribute(ERROR, "Usuario no autenticado correctamente.");
            return REDIRECT; // O a la página de login
        }

        Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
        if (establecimientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR, "Establecimiento no encontrado.");
            return REDIRECT_MIS_RESERVAS;
        }
        Establecimiento establecimiento = establecimientoOpt.get();

        // Verificar si el usuario tiene este establecimiento asignado
        boolean asignado = usuario.getLstEstablecimientos().stream().anyMatch(e -> e.getId().equals(establecimientoId));
        if (!asignado) {
            redirectAttributes.addFlashAttribute(ERROR, "No tiene permiso para reservar en este establecimiento.");
            return REDIRECT_MIS_RESERVAS;
        }

        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(fechaStr);
            hora = LocalTime.parse(horaStr);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Formato de fecha u hora inválido.");
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimientoId;
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
            redirectAttributes.addFlashAttribute(ERROR, "La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida.");
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimientoId;
        }

        // TODO: Validación de capacidad (Asegurarse que no se exceda el aforo)
        // if (reservasExistentes >= establecimiento.getAforo()) {
        //    redirectAttributes.addFlashAttribute(ERROR, "Aforo completo para la fecha y hora seleccionada.");
        //    return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimientoId;
        // }

        reserva.setUsuario(usuario);
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(fechaReserva);

        try {
            reservaRepo.save(reserva);
            redirectAttributes.addFlashAttribute(EXITO, "Reserva creada correctamente para el " + fechaReserva.toLocalDate() + " a las " + fechaReserva.toLocalTime());
            return REDIRECT_MIS_RESERVAS; // O a una página de confirmación/listado de mis reservas
        } catch (Exception e) {
            // Podría ser una DataIntegrityViolationException si hay constraints, u otra.
            redirectAttributes.addFlashAttribute(ERROR, "Error al guardar la reserva: " + e.getMessage());
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimientoId;
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
                redirectAttributes.addFlashAttribute(ERROR, "Usuario no autenticado correctamente.");
                model.addAttribute(ERROR, "Usuario no encontrado");
                return ERROR;
            }

            // Obtener establecimientos activos para mostrar
            List<Establecimiento> establecimientos = establecimientoService.findAll();
            model.addAttribute("establecimientos", establecimientos);
            
            return "reservas/misreservas";

        } catch (Exception e) {
        	log.error("Error al mostrar mis reservas: {}", e.getMessage(), e);
            model.addAttribute(ERROR, "Error interno del servidor");
            return ERROR;
        }
    }
}
