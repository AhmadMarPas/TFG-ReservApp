package es.ubu.reservapp.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
import es.ubu.reservapp.util.SlotReservaUtil;
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

    // Constantes para rutas y mensajes
    private static final String REDIRECT = "redirect:/";
    private static final String REDIRECT_CALENDARIO = "reservas/calendario_reserva";
    private static final String REDIRECT_MIS_RESERVAS = REDIRECT + "misreservas";
    private static final String REDIRECT_RESERVAS_ESTABLECIMIENTO = REDIRECT + "reservas/establecimiento/";
    private static final String ERROR = "error";
    private static final String EXITO = "exito";
    private static final String MIS_RESERVAS_VIEW = "reservas/misreservas";
    
    // Servicios inyectados
    private final SessionData sessionData;
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
        // Validar usuario autenticado
        String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
        if (errorUsuario != null) return errorUsuario;

        // Validar y obtener establecimiento
        Optional<Establecimiento> establecimientoOpt = validarEstablecimiento(establecimientoId, redirectAttributes);
        if (establecimientoOpt.isEmpty()) return REDIRECT_MIS_RESERVAS;
        Establecimiento establecimiento = establecimientoOpt.get();

        // Validar permisos del usuario
        String errorPermisos = validarPermisoEstablecimiento(establecimiento, redirectAttributes);
        if (errorPermisos != null) {
        	return errorPermisos;
        }

        // Preparar datos para la vista
        prepararDatosCalendario(establecimiento, model);
        
        return REDIRECT_CALENDARIO;
    }

    /**
     * Crea una nueva reserva para un establecimiento específico.
     *
     * @param reserva Reserva a crear.
     * @param establecimientoId ID del establecimiento.
     * @param fechaStr Fecha de la reserva como String (formato ISO).
     * @param horaInicioStr Hora de inicio como String.
     * @param horaFinStr Hora de fin como String.
     * @param slotSeleccionado Slot seleccionado como String.
     * @param redirectAttributes Atributos para redirección con mensajes flash.
     * @return Redirección a la página de reservas o error.
     */
    @PostMapping("/crear")
    public String crearReserva(@ModelAttribute Reserva reserva,
                               @RequestParam("establecimientoId") Integer establecimientoId,
                               @RequestParam("fecha") String fechaStr,
                               @RequestParam(value = "horaInicio", required = false) String horaInicioStr,
                               @RequestParam(value = "horaFin", required = false) String horaFinStr,
                               @RequestParam(value = "slotSeleccionado", required = false) String slotSeleccionado,
                               RedirectAttributes redirectAttributes) {
        
        log.info("Creando reserva para establecimiento: {}, fecha: {}, horaInicio: {}, horaFin: {}, slot: {}", 
                establecimientoId, fechaStr, horaInicioStr, horaFinStr, slotSeleccionado);

        // Pipeline de validaciones
		String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
		if (errorUsuario != null) {
			return errorUsuario;
		}

		Optional<Establecimiento> establecimientoResult = validarEstablecimiento(establecimientoId, redirectAttributes);
		if (establecimientoResult.isEmpty()) {
			return REDIRECT_MIS_RESERVAS;
		}
		Establecimiento establecimiento = establecimientoResult.get();

		String errorPermisos = validarPermisoEstablecimiento(establecimiento, redirectAttributes);
		if (errorPermisos != null) {
			return errorPermisos;
		}

		HorarioReserva horario = parsearHorarios(fechaStr, horaInicioStr, horaFinStr, slotSeleccionado, redirectAttributes);
		if (horario == null) {
			return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimientoId;
		}

		String errorFranja = validarFranjaHoraria(establecimiento, horario, redirectAttributes);
		if (errorFranja != null) {
			return errorFranja;
		}

		return guardarReserva(reserva, establecimiento, horario, redirectAttributes);
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
            Usuario usuario = sessionData.getUsuario();
            
            if (usuario == null) {
                log.error("Usuario de la sesión no encontrado");
                redirectAttributes.addFlashAttribute(ERROR, "Usuario no autenticado correctamente.");
                model.addAttribute(ERROR, "Usuario no encontrado");
                return ERROR;
            }

            List<Establecimiento> establecimientos = establecimientoService.findAll();
            model.addAttribute("establecimientos", establecimientos);
            
            return MIS_RESERVAS_VIEW;

        } catch (Exception e) {
            log.error("Error al mostrar mis reservas: {}", e.getMessage(), e);
            model.addAttribute(ERROR, "Error interno del servidor");
            return ERROR;
        }
    }

    // ================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ================================

    /**
     * Valida que el usuario esté autenticado.
     */
    private String validarUsuarioAutenticado(RedirectAttributes redirectAttributes) {
        if (sessionData.getUsuario() == null) {
            redirectAttributes.addFlashAttribute(ERROR, "Usuario no autenticado correctamente.");
            return REDIRECT;
        }
        return null;
    }

    /**
     * Comprueba si el establecimiento con el ID proporcionado existe y lo devuelve.
     */
    private Optional<Establecimiento> validarEstablecimiento(Integer establecimientoId, RedirectAttributes redirectAttributes) {
        Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
        if (establecimientoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR, "Establecimiento no encontrado.");
        } else if (!establecimientoOpt.get().isActivo()) {
        	redirectAttributes.addFlashAttribute(ERROR, "El establecimiento no está activo.");
        }
        return establecimientoOpt;
    }

    /**
     * Valida que el usuario tenga permiso para reservar en el establecimiento.
     */
    private String validarPermisoEstablecimiento(Establecimiento establecimiento, RedirectAttributes redirectAttributes) {
        Usuario usuario = sessionData.getUsuario();
        boolean asignado = usuario.getLstEstablecimientos().stream().anyMatch(e -> e.getId().equals(establecimiento.getId()));
        
        if (!asignado) {
            redirectAttributes.addFlashAttribute(ERROR, "No tiene permiso para reservar en este establecimiento.");
            return REDIRECT_MIS_RESERVAS;
        }
        return null;
    }

    /**
     * Parsea y valida los horarios de la reserva.
     * 
     * @param fechaStr
     * @param horaInicioStr
     * @param horaFinStr
     * @param slotSeleccionado
     * @param redirectAttributes
     */
    private HorarioReserva parsearHorarios(String fechaStr, String horaInicioStr, String horaFinStr, String slotSeleccionado, RedirectAttributes redirectAttributes) {
        try {
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime[] horas = extraerHoras(horaInicioStr, horaFinStr, slotSeleccionado, redirectAttributes);
            
            if (horas == null) return null;
            
            LocalTime horaInicio = horas[0];
            LocalTime horaFin = horas[1];

            if (!horaFin.isAfter(horaInicio)) {
                redirectAttributes.addFlashAttribute(ERROR, "La hora de fin debe ser posterior a la hora de inicio.");
                return null;
            }

            return new HorarioReserva(fecha, horaInicio, horaFin);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Formato de fecha u hora inválido.");
            return null;
        }
    }

    /**
     * Extrae las horas de inicio y fin desde los parámetros o slot seleccionado.
     * 
     * @param horaInicioStr
     * @param horaFinStr
     * @param slotSeleccionado
     * @param redirectAttributes
     */
    private LocalTime[] extraerHoras(String horaInicioStr, String horaFinStr, String slotSeleccionado, RedirectAttributes redirectAttributes) {
        if (slotSeleccionado != null && !slotSeleccionado.trim().isEmpty()) {
            return procesarSlotSeleccionado(slotSeleccionado, redirectAttributes);
        } else {
            return procesarHorasDirectas(horaInicioStr, horaFinStr, redirectAttributes);
        }
    }

    /**
     * Procesa un slot seleccionado en formato "HH:mm - HH:mm".
     * 
     * @param slotSeleccionado
     * @param redirectAttributes
     */
    private LocalTime[] procesarSlotSeleccionado(String slotSeleccionado, RedirectAttributes redirectAttributes) {
        String[] partes = slotSeleccionado.split(" - ");
        if (partes.length != 2) {
            redirectAttributes.addFlashAttribute(ERROR, "Formato de slot inválido");
            return null;
        }
        return new LocalTime[]{LocalTime.parse(partes[0]), LocalTime.parse(partes[1])};
    }

    /**
     * Procesa horas proporcionadas directamente.
     * 
     * @param horaInicioStr
     * @param horaFinStr
     * @param redirectAttributes
     */
    private LocalTime[] procesarHorasDirectas(String horaInicioStr, String horaFinStr, RedirectAttributes redirectAttributes) {
        if (horaInicioStr == null || horaFinStr == null) {
            redirectAttributes.addFlashAttribute(ERROR, "Debe especificar hora de inicio y fin");
            return null;
        }
        return new LocalTime[]{LocalTime.parse(horaInicioStr), LocalTime.parse(horaFinStr)};
    }

    /**
     * Valida la franja horaria.
     * 
     * @param establecimiento
     * @param horario
     * @param redirectAttributes
     */
    private String validarFranjaHoraria(Establecimiento establecimiento, HorarioReserva horario, RedirectAttributes redirectAttributes) {
        DayOfWeek diaSemanaReserva = LocalDateTime.of(horario.getFecha(), horario.getHoraInicio()).getDayOfWeek();
        
        boolean dentroDeFranja = establecimiento.getFranjasHorarias().stream().anyMatch(franja -> estaEnFranja(franja, diaSemanaReserva, horario));

        if (!dentroDeFranja) {
            redirectAttributes.addFlashAttribute(ERROR, "La hora seleccionada está fuera del horario de apertura del establecimiento para ese día o no es válida.");
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimiento.getId();
        }
        
        return null;
    }

    /**
     * Verifica si el horario está dentro de la franja horaria especificada.
     * 
     * @param franja
     * @param diaSemana
     * @param horario
     */
    private boolean estaEnFranja(FranjaHoraria franja, DayOfWeek diaSemana, HorarioReserva horario) {
        return franja.getDiaSemana() == diaSemana
                && !horario.getHoraInicio().isBefore(franja.getHoraInicio())
                && !horario.getHoraFin().isAfter(franja.getHoraFin());
    }

    /**
     * Guarda la reserva en el repositorio.
     * 
     * @param reserva
     * @param establecimiento
     * @param horario
     * @param redirectAttributes
     */
    private String guardarReserva(Reserva reserva, Establecimiento establecimiento, HorarioReserva horario, RedirectAttributes redirectAttributes) {
        configurarReserva(reserva, establecimiento, horario);

        try {
            reservaRepo.save(reserva);
            redirectAttributes.addFlashAttribute(EXITO, construirMensajeExito(horario));
            return REDIRECT_MIS_RESERVAS;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Error al guardar la reserva: " + e.getMessage());
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimiento.getId();
        }
    }

    /**
     * Configura los datos de la reserva antes de guardarla.
     * 
     * @param reserva
     * @param establecimiento
     * @param horario
     */
    private void configurarReserva(Reserva reserva, Establecimiento establecimiento, HorarioReserva horario) {
        reserva.setUsuario(sessionData.getUsuario());
        reserva.setEstablecimiento(establecimiento);
        reserva.setFechaReserva(LocalDateTime.of(horario.getFecha(), horario.getHoraInicio()));
        reserva.setHoraFin(horario.getHoraFin());
    }

    /**
     * Construye el mensaje de éxito para la reserva creada.
     * 
     * @param horario
     */
    private String construirMensajeExito(HorarioReserva horario) {
        return String.format("Reserva creada correctamente para el %s de %s a %s", 
                horario.getFecha(), horario.getHoraInicio(), horario.getHoraFin());
    }

    // ================================
    // MÉTODOS PRIVADOS DE PREPARACIÓN DE DATOS
    // ================================

    /**
     * Prepara todos los datos necesarios para mostrar el calendario de reservas.
     * 
     * @param establecimiento
     * @param model
     */
    private void prepararDatosCalendario(Establecimiento establecimiento, Model model) {
        Usuario usuario = sessionData.getUsuario();
        
        // Obtener franjas horarias activas
        List<FranjaHoraria> franjasActivas = obtenerFranjasActivas(establecimiento);
        
        // Obtener reservas del usuario
        ReservasUsuario reservasUsuario = obtenerReservasUsuario(usuario, establecimiento);
        
        // Generar slots si es necesario
        SlotsData slotsData = generarSlotsData(establecimiento, franjasActivas);
        
        // Configurar modelo
        configurarModeloCalendario(model, establecimiento, franjasActivas, reservasUsuario, slotsData);
    }

    /**
     * Obtiene las franjas horarias activas del establecimiento.
     */
    private List<FranjaHoraria> obtenerFranjasActivas(Establecimiento establecimiento) {
        return establecimiento.getFranjasHorarias().stream()
                .filter(fh -> establecimiento.isActivo())
                .sorted(Comparator.comparing(FranjaHoraria::getDiaSemana)
                        .thenComparing(FranjaHoraria::getHoraInicio))
                .toList();
    }

    /**
     * Obtiene las reservas pasadas y futuras del usuario para el establecimiento.
     * 
     * @param establecimiento
     */
    private ReservasUsuario obtenerReservasUsuario(Usuario usuario, Establecimiento establecimiento) {
        LocalDateTime fechaActual = LocalDateTime.now();
        
        List<Reserva> reservasPasadas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
        List<Reserva> reservasFuturas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
        
        // Ordenar reservas
        reservasPasadas.sort(Comparator.comparing(Reserva::getFechaReserva).reversed());
        reservasFuturas.sort(Comparator.comparing(Reserva::getFechaReserva));
        
        return new ReservasUsuario(reservasPasadas, reservasFuturas);
    }

    /**
     * Genera los datos de slots si es necesario.
     * 
     * @param establecimiento
     * @param franjasActivas
     */
    private SlotsData generarSlotsData(Establecimiento establecimiento, List<FranjaHoraria> franjasActivas) {
        boolean requiereSlots = SlotReservaUtil.requiereSlotsPredefinidos(establecimiento);
        Map<DayOfWeek, List<SlotReservaUtil.SlotTiempo>> slotsDisponibles = new EnumMap<>(DayOfWeek.class);
        
        if (requiereSlots) {
            for (FranjaHoraria franja : franjasActivas) {
                List<SlotReservaUtil.SlotTiempo> slots = SlotReservaUtil.generarSlotsDisponibles(establecimiento, franja);
                slotsDisponibles.put(franja.getDiaSemana(), slots);
            }
        }
        
        return new SlotsData(requiereSlots, slotsDisponibles);
    }

    /**
     * Configura el modelo con todos los datos necesarios para el calendario.
     * 
     * @param model
     * @param establecimiento
     * @param franjasActivas
     * @param reservasUsuario
     * @param slotsData
     */
    private void configurarModeloCalendario(Model model, Establecimiento establecimiento, 
                                           List<FranjaHoraria> franjasActivas, ReservasUsuario reservasUsuario, 
                                           SlotsData slotsData) {
        model.addAttribute("establecimiento", establecimiento);
        model.addAttribute("franjasHorarias", franjasActivas);
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("reservasPasadas", reservasUsuario.getPasadas());
        model.addAttribute("reservasFuturas", reservasUsuario.getFuturas());
        model.addAttribute("requiereSlotsPredefinidos", slotsData.isRequiereSlots());
        model.addAttribute("slotsDisponibles", slotsData.getSlotsDisponibles());
    }

    // ================================
    // CLASES AUXILIARES
    // ================================

    /**
     * Clase auxiliar para encapsular los datos de horario de una reserva.
     */
    private static class HorarioReserva {
        private final LocalDate fecha;
        private final LocalTime horaInicio;
        private final LocalTime horaFin;

        public HorarioReserva(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
            this.fecha = fecha;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }

        public LocalDate getFecha() { return fecha; }
        public LocalTime getHoraInicio() { return horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
    }

    /**
     * Clase auxiliar para encapsular las reservas de un usuario.
     */
    private static class ReservasUsuario {
        private final List<Reserva> pasadas;
        private final List<Reserva> futuras;

        public ReservasUsuario(List<Reserva> pasadas, List<Reserva> futuras) {
            this.pasadas = pasadas;
            this.futuras = futuras;
        }

        public List<Reserva> getPasadas() { return pasadas; }
        public List<Reserva> getFuturas() { return futuras; }
    }

    /**
     * Clase auxiliar para encapsular los datos de slots.
     */
    private static class SlotsData {
        private final boolean requiereSlots;
        private final Map<DayOfWeek, List<SlotReservaUtil.SlotTiempo>> slotsDisponibles;

        public SlotsData(boolean requiereSlots, Map<DayOfWeek, List<SlotReservaUtil.SlotTiempo>> slotsDisponibles) {
            this.requiereSlots = requiereSlots;
            this.slotsDisponibles = slotsDisponibles;
        }

        public boolean isRequiereSlots() { return requiereSlots; }

		public Map<DayOfWeek, List<SlotReservaUtil.SlotTiempo>> getSlotsDisponibles() {
			return slotsDisponibles;
		}
    }
}