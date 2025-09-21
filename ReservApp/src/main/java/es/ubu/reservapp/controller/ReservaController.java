package es.ubu.reservapp.controller;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.ConvocadoPK;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.shared.SessionData;
import es.ubu.reservapp.service.ConvocatoriaService;
import es.ubu.reservapp.service.EmailService;
import es.ubu.reservapp.service.EstablecimientoService;
import es.ubu.reservapp.service.ReservaService;
import es.ubu.reservapp.service.UsuarioService;
import es.ubu.reservapp.util.SlotReservaUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para la gestión de Reservas.
 * 
 * OPTIMIZACIONES IMPLEMENTADAS:
 * - buscarUsuarios(): Eliminada carga de todos los usuarios en memoria, usa consulta optimizada en BD
 * - crearConvocatorias(): Evita N+1 queries cargando usuarios en lote
 * - gestionarConvocatorias(): Evita N+1 queries cargando usuarios en lote
 * - mostrarMisReservas(): Solo carga establecimientos activos asignados al usuario
 * - obtenerReservasUsuario(): Limita reservas pasadas para mejorar rendimiento
 * 
 * FUNCIONALIDAD DE AFORO IMPLEMENTADA:
 * - Validación de disponibilidad considerando aforo del establecimiento
 * - Slots deshabilitados cuando se alcanza el aforo máximo
 * - Información de franjas horarias disponibles para reservas de libre selección
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.2 - Con funcionalidad de aforo
 * @since 1.0
 */
@Controller
@Slf4j
@RequestMapping("/misreservas")
@PreAuthorize("isAuthenticated()")
public class ReservaController {

    // Constantes para rutas y mensajes
	private static final String FORMATO_HORAS = "HH:mm";
	private static final String ESTABLECIMIENTO = "establecimiento";
	private static final String PERIODOS_LIBRES = "periodosLibres";
    private static final String REDIRECT = "redirect:/";
    private static final String REDIRECT_CALENDARIO = "reservas/calendario_reserva";
    private static final String REDIRECT_MIS_RESERVAS = REDIRECT + "misreservas";
    private static final String REDIRECT_RESERVAS_ESTABLECIMIENTO = REDIRECT + "misreservas/establecimiento/";
    private static final String ERROR = "error";
    private static final String EXITO = "exito";
    private static final String RESERVA_NO_ENCONTRADA = "Reserva no encontrada.";
    private static final String MIS_RESERVAS_VIEW = "reservas/misreservas";
    private static final String REDIRECT_MIS_RESERVAS_EDITAR = REDIRECT_MIS_RESERVAS + "/editar/";
    
    // Servicios inyectados
    private final SessionData sessionData;
    private final EstablecimientoService establecimientoService;
    private final ReservaService reservaService;
    private final ConvocatoriaService convocatoriaService;
    private final UsuarioService usuarioService;
    private final EmailService emailService;

    /**
	 * Constructor del controlador que inyecta los servicios necesarios.
	 *
	 * @param sessionData Datos de la sesión.
	 * @param establecimientoService Servicio para gestionar establecimientos.
	 * @param reservaService Repositorio de reservas.
	 * @param convocatoriaService Servicio para gestionar convocatorias.
	 * @param usuarioService Servicio para gestionar usuarios.
	 * @param emailService Servicio para envío de correos electrónicos.
	 */
    public ReservaController(SessionData sessionData, EstablecimientoService establecimientoService, ReservaService reservaService, ConvocatoriaService convocatoriaService, UsuarioService usuarioService, EmailService emailService) {
    	this.sessionData = sessionData;
        this.establecimientoService = establecimientoService;
        this.reservaService = reservaService;
        this.convocatoriaService = convocatoriaService;
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }
    
    /**
     * Muestra el calendario mensual de disponibilidad para un establecimiento específico.
     *
     * @param establecimientoId ID del establecimiento.
     * @param mes Mes seleccionado (opcional, por defecto el mes actual).
     * @param anio Año seleccionado (opcional, por defecto el año actual).
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para redirección con mensajes flash.
     * @return Nombre de la vista a mostrar.
     */
    @GetMapping("/calendario/{id}")
    public String mostrarCalendarioMensual(@PathVariable("id") Integer establecimientoId,
                                         @RequestParam(name = "mes", required = false) Integer mes,
                                         @RequestParam(name = "anio", required = false) Integer anio,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            log.info("=== INICIO: Solicitud de calendario mensual para establecimiento {} ===", establecimientoId);
            
            String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
            if (errorUsuario != null) {
                log.warn("Usuario no autenticado para calendario mensual");
                return errorUsuario;
            }

            Optional<Establecimiento> establecimientoOpt = validarEstablecimiento(establecimientoId, redirectAttributes);
            if (establecimientoOpt.isEmpty()) {
                log.warn("Establecimiento {} no encontrado", establecimientoId);
                return REDIRECT_MIS_RESERVAS;
            }
            Establecimiento establecimiento = establecimientoOpt.get();

            String errorPermisos = validarPermisoEstablecimiento(establecimiento, redirectAttributes);
            if (errorPermisos != null) {
                log.warn("Usuario sin permisos para establecimiento {}", establecimientoId);
                return errorPermisos;
            }

            log.info("Validaciones completadas. Preparando datos del calendario...");

            prepararDatosCalendarioMensual(establecimiento, mes, anio, model);
            if (model.containsAttribute(ERROR)) {
				log.warn("Error al preparar datos del calendario: {}", model.getAttribute(ERROR));
				redirectAttributes.addFlashAttribute(ERROR, "Error al cargar el calendario");
				return REDIRECT_MIS_RESERVAS;
			}
            
            log.info("Calendario mensual preparado para establecimiento: {}, mes: {}, año: {}", establecimientoId, mes, anio);
            
            log.info("=== FIN: Redirigiendo a template calendario_mensual_usuario ===");
            return "reservas/calendario_mensual_usuario";
        } catch (Exception e) {
            log.error("Error al mostrar calendario mensual para establecimiento {}: {}", establecimientoId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR, "Error al cargar el calendario: " + e.getMessage());
            return REDIRECT_MIS_RESERVAS;
        }
    }

    /**
     * Modificado para aceptar fecha preseleccionada desde el calendario mensual.
     *
     * @param establecimientoId ID del establecimiento.
     * @param fechaPreseleccionada Fecha preseleccionada desde el calendario mensual (opcional).
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para redirección con mensajes flash.
     * @return Nombre de la vista a mostrar.
     */
    @GetMapping("/establecimiento/{id}")
	public String mostrarCalendarioReserva(@PathVariable("id") Integer establecimientoId,
			@RequestParam(value = "fecha", required = false) String fechaPreseleccionada, Model model,
			RedirectAttributes redirectAttributes, HttpServletRequest request) {

		log.info("=== INICIO: mostrarCalendarioReserva ===");
		log.info("Establecimiento ID: {}", establecimientoId);
		log.info("Fecha preseleccionada recibida: '{}'", fechaPreseleccionada);
		log.info("Query string completa: {}", request.getQueryString());
		log.info("URL completa: {}",
				request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

		String errorValidacion = realizarValidacionesIniciales(establecimientoId, redirectAttributes);
		if (errorValidacion != null) {
			return errorValidacion;
		}

		Establecimiento establecimiento = obtenerEstablecimientoValidado(establecimientoId, redirectAttributes);
		prepararDatosCalendario(establecimiento, model);
		model.addAttribute(PERIODOS_LIBRES, new ArrayList<>());

		procesarFechaPreseleccionada(fechaPreseleccionada, establecimiento, model);

		return REDIRECT_CALENDARIO;
	}

    /**
	 * Realiza las validaciones iniciales comunes para los endpoints que requieren establecimiento.
	 * 
	 * @param establecimientoId ID del establecimiento a validar.
	 * @param redirectAttributes Atributos para redirección con mensajes flash.
	 * @return Mensaje de error si hay alguna validación fallida, o null si ttodo es correcto.
	 */
	private String realizarValidacionesIniciales(Integer establecimientoId, RedirectAttributes redirectAttributes) {
		String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
		if (errorUsuario != null) {
			return errorUsuario;
		}

		Optional<Establecimiento> establecimientoOpt = validarEstablecimiento(establecimientoId, redirectAttributes);
		if (establecimientoOpt.isEmpty()) {
			return REDIRECT_MIS_RESERVAS;
		}

		return validarPermisoEstablecimiento(establecimientoOpt.get(), redirectAttributes);
	}

	/**
	 * Prepara los datos necesarios para el calendario mensual.
	 * 
	 * @param establecimientoId Establecimiento para el cual se muestra el calendario.
	 * @param redirectAttributes Atributos para redirección con mensajes flash.
	 */
	private Establecimiento obtenerEstablecimientoValidado(Integer establecimientoId, RedirectAttributes redirectAttributes) {
	    Optional<Establecimiento> establecimientoOpt = validarEstablecimiento(establecimientoId, redirectAttributes);
	    if (establecimientoOpt.isPresent()) {
	        return establecimientoOpt.get();
	    }
	    throw new IllegalArgumentException("Establecimiento no encontrado");
	}

	/**
	 * Prepara los datos necesarios para el calendario mensual.
	 * 
	 * @param establecimiento Establecimiento para el cual se muestra el calendario.
	 * @param mes Mes seleccionado (1-12), o null para el mes actual.
	 * @param anio Año seleccionado, o null para el año actual.
	 * @param model Modelo para pasar datos a la vista.
	 */
	private void procesarFechaPreseleccionada(String fechaPreseleccionada, Establecimiento establecimiento, Model model) {
		if (esFechaPreseleccionadaValida(fechaPreseleccionada)) {
			return;
		}

		try {
			log.info("=== PROCESANDO FECHA PRESELECCIONADA: '{}' ===", fechaPreseleccionada);
			LocalDate fecha = LocalDate.parse(fechaPreseleccionada);
			log.info("Fecha parseada correctamente: {}", fecha);

			procesarFechaParseada(fecha, establecimiento, model, fechaPreseleccionada);

		} catch (Exception e) {
			log.error("Error al procesar fecha preseleccionada '{}': {}", fechaPreseleccionada, e.getMessage());
		}
	}

	/**
	 * Prepara los datos necesarios para el calendario mensual.
	 * 
	 * @param establecimiento Establecimiento para el cual se muestra el calendario.
	 * @param mes Mes seleccionado (1-12), o null para el mes actual.
	 * @param anio Año seleccionado, o null para el año actual.
	 * @param model Modelo para pasar datos a la vista.
	 */
	private boolean esFechaPreseleccionadaValida(String fechaPreseleccionada) {
		boolean esInvalida = fechaPreseleccionada == null || fechaPreseleccionada.trim().isEmpty();
		if (esInvalida) {
			log.info("No hay fecha preseleccionada o está vacía: '{}'", fechaPreseleccionada);
		}
		return esInvalida;
	}

	/**
	 * Prepara los datos necesarios para el calendario mensual.
	 * 
	 * @param establecimiento Establecimiento para el cual se muestra el calendario.
	 * @param mes Mes seleccionado (1-12), o null para el mes actual.
	 * @param anio Año seleccionado, o null para el año actual.
	 * @param model Modelo para pasar datos a la vista.
	 */
	private void procesarFechaParseada(LocalDate fecha, Establecimiento establecimiento, Model model,
			String fechaPreseleccionada) {
		if (fecha.isBefore(LocalDate.now())) {
			log.warn("Fecha preseleccionada es pasada: {}", fecha);
			return;
		}

		configurarFechaEnModelo(fecha, fechaPreseleccionada, model);
		procesarPeriodosLibresSiEsNecesario(establecimiento, fecha, model);
	}

	/**
	 * Prepara los datos necesarios para el calendario mensual.
	 * 
	 * @param establecimiento Establecimiento para el cual se muestra el calendario.
	 * @param mes Mes seleccionado (1-12), o null para el mes actual.
	 * @param anio Año seleccionado, o null para el año actual.
	 * @param model Modelo para pasar datos a la vista.
	 */
	private void configurarFechaEnModelo(LocalDate fecha, String fechaPreseleccionada, Model model) {
		model.addAttribute("fechaPreseleccionada", fechaPreseleccionada);
		model.addAttribute("fechaFormateada", fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		log.info("Fecha preseleccionada desde calendario mensual: {}", fechaPreseleccionada);
	}

	/**
	 * Prepara los datos necesarios para el calendario mensual.
	 * 
	 * @param establecimiento Establecimiento para el cual se muestra el calendario.
	 * @param mes Mes seleccionado (1-12), o null para el mes actual.
	 * @param anio Año seleccionado, o null para el año actual.
	 * @param model Modelo para pasar datos a la vista.
	 */
	private void procesarPeriodosLibresSiEsNecesario(Establecimiento establecimiento, LocalDate fecha, Model model) {
		Integer duracion = establecimiento.getDuracionReserva();
		log.info("Duración del establecimiento: {}", duracion);

		if (tieneEstrategiaSinDuracionFija(duracion)) {
			procesarEstablecimientoSinDuracionFija(establecimiento, fecha, model);
		} else {
			procesarEstablecimientoConDuracionFija(duracion, model);
		}
	}

	/**
	 * Verifica si el establecimiento utiliza la estrategia de reserva sin duración fija.
	 * 
	 * @param duracion Duración de la reserva en minutos (puede ser null).
	 * @return true si la estrategia es sin duración fija, false si es con duración fija.
	 */
	private boolean tieneEstrategiaSinDuracionFija(Integer duracion) {
		return duracion == null || duracion <= 0;
	}

	/**
	 * Obtiene los períodos libres para un establecimiento en una fecha específica.
	 * 
	 * @param establecimiento Establecimiento para el cual se obtienen los períodos.
	 * @param fecha Fecha para la cual se obtienen los períodos.
	 * @return Lista de períodos libres.
	 */
	private void procesarEstablecimientoSinDuracionFija(Establecimiento establecimiento, LocalDate fecha, Model model) {
		log.info("Establecimiento SIN duración fija - calculando períodos libres");
		List<PeriodoLibre> periodosLibres = obtenerPeriodosLibres(establecimiento, fecha);
		model.addAttribute(PERIODOS_LIBRES, periodosLibres);

		logPeriodosLibresCalculados(periodosLibres, fecha);
	}

	/**
	 * Obtiene los períodos libres para un establecimiento en una fecha específica.
	 * 
	 * @param establecimiento Establecimiento para el cual se obtienen los períodos.
	 * @param fecha Fecha para la cual se obtienen los períodos.
	 * @return Lista de períodos libres.
	 */
	private void procesarEstablecimientoConDuracionFija(Integer duracion, Model model) {
		model.addAttribute(PERIODOS_LIBRES, new ArrayList<>());
		log.info("Establecimiento con duración fija ({} min) - no se muestran períodos libres", duracion);
	}

	/**
	 * Obtiene los períodos libres para un establecimiento en una fecha específica.
	 * 
	 * @param establecimiento Establecimiento para el cual se obtienen los períodos.
	 * @param fecha Fecha para la cual se obtienen los períodos.
	 * @return Lista de períodos libres.
	 */
	private void logPeriodosLibresCalculados(List<PeriodoLibre> periodosLibres, LocalDate fecha) {
		log.info("=== PERÍODOS LIBRES CALCULADOS: {} períodos para {} ===", periodosLibres.size(), fecha);
		for (int i = 0; i < periodosLibres.size(); i++) {
			PeriodoLibre periodo = periodosLibres.get(i);
			log.info("  Período {}: {} - {} ({} espacios)", i + 1, periodo.getHoraInicioFormateada(),
					periodo.getHoraFinFormateada(), periodo.getEspaciosDisponibles());
		}
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
                               @RequestParam(value = "enlaceReunion", required = false) String enlaceReunion,
                               @RequestParam(value = "observaciones", required = false) String observaciones,
                               @RequestParam(value = "usuariosConvocados", required = false) String[] usuariosConvocados,
                               RedirectAttributes redirectAttributes) {
        
        log.info("Creando reserva para establecimiento: {}, fecha: {}, horaInicio: {}, horaFin: {}, slot: {}", 
                establecimientoId, fechaStr, horaInicioStr, horaFinStr, slotSeleccionado);

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

		String errorAforo = validarDisponibilidadAforo(establecimiento, horario, null, redirectAttributes);
		if (errorAforo != null) {
			return errorAforo;
		}

		Convocatoria convocatoria = new Convocatoria();
		convocatoria.setReserva(reserva);
		convocatoria.setEnlace(enlaceReunion);
		convocatoria.setObservaciones(observaciones);
		reserva.setConvocatoria(convocatoria);
		return guardarReservaConConvocatoria(reserva, establecimiento, horario, usuariosConvocados, redirectAttributes);
    }

    /**
     * Muestra las reservas del usuario actual.
     * OPTIMIZADO: Solo carga establecimientos asignados al usuario.
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

            model.addAttribute("establecimientos", usuario.getLstEstablecimientos());
            
            return MIS_RESERVAS_VIEW;

        } catch (Exception e) {
            log.error("Error al mostrar mis reservas: {}", e.getMessage(), e);
            model.addAttribute(ERROR, "Error interno del servidor");
            return ERROR;
        }
    }

    /**
     * Endpoint AJAX para obtener slots disponibles para una fecha específica.
     * 
     * @param establecimientoId ID del establecimiento
     * @param fecha Fecha para la cual obtener los slots
     * @param reservaId ID de la reserva a excluir del cálculo (opcional, para ediciones)
     * @return ResponseEntity con los slots disponibles
     */
    @GetMapping("/slots-disponibles")
    @ResponseBody
    public ResponseEntity<SlotsDisponiblesResponse> obtenerSlotsDisponibles(
            @RequestParam Integer establecimientoId,
            @RequestParam String fecha,
            @RequestParam(required = false) Integer reservaId) {
        
        try {
            if (sessionData.getUsuario() == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
            if (establecimientoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Establecimiento establecimiento = establecimientoOpt.get();
            LocalDate fechaReserva = LocalDate.parse(fecha);
            DayOfWeek diaSemana = fechaReserva.getDayOfWeek();

            LocalDateTime inicioDelDia = fechaReserva.atStartOfDay();
            LocalDateTime finDelDia = fechaReserva.atTime(23, 59, 59);
            List<Reserva> reservasDelDia = reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento, inicioDelDia, finDelDia);
            
            if (reservaId != null) {
                reservasDelDia = reservasDelDia.stream()
                    .filter(reserva -> !reserva.getId().equals(reservaId)).toList();
            }

            List<SlotReservaUtil.SlotTiempo> slotsDisponibles = new ArrayList<>();
            
            for (FranjaHoraria franja : establecimiento.getFranjasHorarias()) {
                if (franja.getDiaSemana().equals(diaSemana)) {
                    List<SlotReservaUtil.SlotTiempo> slotsFranja = SlotReservaUtil.generarSlotsConDisponibilidad(establecimiento, franja, reservasDelDia);
                    slotsDisponibles.addAll(slotsFranja);
                }
            }

            slotsDisponibles.sort(Comparator.comparing(SlotReservaUtil.SlotTiempo::getHoraInicio));

            SlotsDisponiblesResponse response = new SlotsDisponiblesResponse();
            response.setSlots(slotsDisponibles);
            response.setAforo(establecimiento.getAforo());
            response.setTieneAforo(establecimiento.getAforo() != null && establecimiento.getAforo() > 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener slots disponibles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint AJAX para obtener franjas horarias disponibles para reservas de libre selección.
     * 
     * @param establecimientoId ID del establecimiento
     * @param fecha Fecha para la cual obtener las franjas
     * @return ResponseEntity con las franjas disponibles
     */
    @GetMapping("/franjas-disponibles")
    @ResponseBody
    public ResponseEntity<FranjasDisponiblesResponse> obtenerFranjasDisponibles(@RequestParam Integer establecimientoId, @RequestParam String fecha) {
        
        try {
            if (sessionData.getUsuario() == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
            if (establecimientoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Establecimiento establecimiento = establecimientoOpt.get();
            LocalDate fechaReserva = LocalDate.parse(fecha);

            List<ReservaService.FranjaDisponibilidad> franjasDisponibles = reservaService.obtenerFranjasDisponibles(establecimiento, fechaReserva);

            FranjasDisponiblesResponse response = new FranjasDisponiblesResponse();
            response.setFranjas(franjasDisponibles);
            response.setAforo(establecimiento.getAforo());
            response.setTieneAforo(establecimiento.getAforo() != null && establecimiento.getAforo() > 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener franjas disponibles: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para buscar usuarios por nombre o ID (AJAX).
     * OPTIMIZADO: Usa consulta de base de datos en lugar de cargar todos los usuarios en memoria.
     * 
     * @param query término de búsqueda
     * @return lista de usuarios que coinciden con la búsqueda
     */
    @GetMapping("/buscar-usuarios")
    @ResponseBody
    public List<UsuarioDTO> buscarUsuarios(@RequestParam String query) {
        if (query == null || query.trim().length() < 2) {
            return new ArrayList<>();
        }
        
        String queryLower = query.toLowerCase().trim();
        
        List<Usuario> usuarios = usuarioService.buscarUsuarioSegunQuery(queryLower);
        
        return usuarios.stream()
                .map(usuario -> new UsuarioDTO(usuario.getId(), 
                    usuario.getNombre() + " " + usuario.getApellidos(), 
                    usuario.getCorreo()))
                .toList();
	}

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
        boolean asignado = usuarioService.establecimientoAsignado(usuario, establecimiento);
        
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
            
            if (horas.length == 0) return null;
            
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
            return new LocalTime[0];
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
            return new LocalTime[0];
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
     * Verifica la disponibilidad considerando el aforo del establecimiento.
     * 
     * @param establecimiento
     * @param horario
     * @param reservaExcluir Reserva a excluir (para ediciones), puede ser null
     * @param redirectAttributes
     */
    private String validarDisponibilidadAforo(Establecimiento establecimiento, HorarioReserva horario, Reserva reservaExcluir, RedirectAttributes redirectAttributes) {
        boolean disponible = reservaService.verificarDisponibilidad(establecimiento, horario.getFecha(), horario.getHoraInicio(), horario.getHoraFin(), reservaExcluir);
        
        if (!disponible) {
            // Obtener información detallada para el mensaje de error
            List<Reserva> reservasSolapadas = reservaService.obtenerReservasSolapadas(establecimiento, horario.getFecha(), horario.getHoraInicio(), horario.getHoraFin());
            
            if (reservaExcluir != null) {
                reservasSolapadas = reservasSolapadas.stream()
                    .filter(r -> !r.getId().equals(reservaExcluir.getId()))
                    .toList();
            }
            
            String mensaje = String.format(
                "No hay disponibilidad para el horario seleccionado. El establecimiento tiene un aforo de %d y ya hay %d reserva(s) en ese horario.",
                establecimiento.getAforo(), reservasSolapadas.size());
            
            redirectAttributes.addFlashAttribute(ERROR, mensaje);
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
     * Guarda la reserva y crea la convocatoria con usuarios invitados.
     * 
     * @param reserva
     * @param establecimiento
     * @param horario
     * @param enlaceReunion
     * @param observaciones
     * @param usuariosConvocados
     * @param redirectAttributes
     */
    private String guardarReservaConConvocatoria(Reserva reserva, Establecimiento establecimiento, HorarioReserva horario, 
                                                String[] usuariosConvocados, RedirectAttributes redirectAttributes) {
        configurarReserva(reserva, establecimiento, horario);

        try {
            Reserva reservaGuardada = reservaService.save(reserva);
            
            if (usuariosConvocados != null && usuariosConvocados.length > 0) {
                crearConvocatorias(reservaGuardada, usuariosConvocados);
            }
            
            enviarCorreoAlPropioUsuario(reservaGuardada);
            
            redirectAttributes.addFlashAttribute(EXITO, construirMensajeExito(horario));
            return REDIRECT_MIS_RESERVAS;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Error al guardar la reserva: " + e.getMessage());
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimiento.getId();
        }
    }

    /**
	 * Envía un correo de confirmación al usuario que crea la reserva.
	 * 
	 * @param reservaGuardada Reserva de la que se envía el correo.
	 */
	private void enviarCorreoAlPropioUsuario(Reserva reservaGuardada) {
		try {
		    emailService.enviarNotificacionReservaCreada(reservaGuardada);
		    log.info("Correo de confirmación de reserva enviado al usuario: {}", reservaGuardada.getUsuario().getCorreo());
		} catch (Exception e) {
		    log.error("Error al enviar correo de confirmación de reserva: {}", e.getMessage(), e);
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
     * Gestiona las convocatorias considerando el soft delete.
     * OPTIMIZADO: Evita N+1 queries cargando todos los usuarios de una vez.
     * Reutiliza convocatorias existentes (incluso las marcadas como inválidas) o crea nuevas.
     */
    private void gestionarConvocatorias(Reserva reserva, String[] usuariosConvocados) {
        convocatoriaService.deleteByReserva(reserva);
        
        if (usuariosConvocados == null || usuariosConvocados.length == 0) {
            return;
        }
        
        List<Convocado> convocatoriasActivas = new ArrayList<>();
        
        Convocatoria nuevaConvocatoria = new Convocatoria();
        nuevaConvocatoria.setReserva(reserva);
        reserva.setConvocatoria(nuevaConvocatoria);
        nuevaConvocatoria.setConvocados(new ArrayList<>());
        log.info("Creando nueva convocatoria para reserva: {}", reserva.getId());
        
        List<String> idsValidos = new ArrayList<>();
        for (String usuarioId : usuariosConvocados) {
            if (usuarioId != null && !usuarioId.trim().isEmpty()) {
                idsValidos.add(usuarioId.trim());
            }
        }
        
        if (!idsValidos.isEmpty()) {
            List<Usuario> usuarios = usuarioService.findUsuariosByIds(idsValidos);
            Map<String, Usuario> usuariosMap = usuarios.stream()
                    .collect(Collectors.toMap(Usuario::getId, u -> u));
            
            for (String usuarioId : idsValidos) {
                Usuario usuario = usuariosMap.get(usuarioId);
                if (usuario != null) {
                    Convocado convocado = new Convocado();
                    ConvocadoPK convocadoId = new ConvocadoPK(reserva.getId(), usuario.getId());
                    convocado.setId(convocadoId);
                    convocado.setConvocatoria(nuevaConvocatoria);
                    convocado.setUsuario(usuario);
                    
                    nuevaConvocatoria.getConvocados().add(convocado);
                    convocatoriasActivas.add(convocado);
                    log.info("Convocado creado para usuario: {} en reserva: {}", usuario.getId(), reserva.getId());
                }
            }
        }
        
        if (!convocatoriasActivas.isEmpty()) {
            convocatoriaService.save(nuevaConvocatoria);
            
            try {
                emailService.enviarNotificacionesConvocatoria(convocatoriasActivas, reserva);
                log.info("Notificaciones de convocatoria enviadas para {} usuarios", convocatoriasActivas.size());
            } catch (Exception e) {
                log.error("Error al enviar notificaciones de convocatoria: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Crea las convocatorias para los usuarios seleccionados.
     * OPTIMIZADO: Evita N+1 queries cargando todos los usuarios de una vez.
     * 
     * @param reserva
     * @param usuariosConvocados
     */
    private void crearConvocatorias(Reserva reserva, String[] usuariosConvocados) {
        List<Convocado> convocatoriasCreadas = new ArrayList<>();
        
        Convocatoria convocatoria = reserva.getConvocatoria();
        if (convocatoria == null) {
            convocatoria = new Convocatoria();
            convocatoria.setReserva(reserva);
            reserva.setConvocatoria(convocatoria);
        }
        
        if (convocatoria.getConvocados() == null) {
            convocatoria.setConvocados(new ArrayList<>());
        }
        
        List<String> idsValidos = new ArrayList<>();
        for (String usuarioId : usuariosConvocados) {
            if (usuarioId != null && !usuarioId.trim().isEmpty()) {
                idsValidos.add(usuarioId.trim());
            }
        }
        
        if (!idsValidos.isEmpty()) {
             List<Usuario> usuarios = usuarioService.findUsuariosByIds(idsValidos);
             Map<String, Usuario> usuariosMap = usuarios.stream()
                     .collect(Collectors.toMap(Usuario::getId, u -> u));
            
            for (String usuarioId : idsValidos) {
                Usuario usuario = usuariosMap.get(usuarioId);
                if (usuario != null) {
                    Convocado convocado = new Convocado();
                    ConvocadoPK convocadoId = new ConvocadoPK(reserva.getId(), usuario.getId());
                    convocado.setId(convocadoId);
                    convocado.setConvocatoria(convocatoria);
                    convocado.setUsuario(usuario);

                    convocatoria.getConvocados().add(convocado);
                    convocatoriasCreadas.add(convocado);
                    log.info("Convocatoria creada para usuario: {} en reserva: {}", usuario.getId(), reserva.getId());
                }
            }
        }
        
        if (!convocatoriasCreadas.isEmpty()) {
            convocatoriaService.save(convocatoria);
        }
        
        enviarConvocatoria(reserva, convocatoriasCreadas);
    }

    /**
     * Envía los correos de notificación de convocatoria.
     * 
     * @param reserva Reserva asociada a la convocatoria.
     * @param convocatoriasCreadas Lista de convocatorias creadas.
	 */
	private void enviarConvocatoria(Reserva reserva, List<Convocado> convocatoriasCreadas) {
        if (!convocatoriasCreadas.isEmpty()) {
            try {
                emailService.enviarNotificacionesConvocatoria(convocatoriasCreadas, reserva);
                log.info("Notificaciones de convocatoria enviadas para {} usuarios", convocatoriasCreadas.size());
            } catch (Exception e) {
                log.error("Error al enviar notificaciones de convocatoria: {}", e.getMessage(), e);
            }
        }
	}

    /**
     * Construye el mensaje de éxito para la reserva creada.
     * 
     * @param horario
     */
    private String construirMensajeExito(HorarioReserva horario) {
        return String.format("Reserva creada correctamente para el %s de %s a %s", horario.getFecha(), horario.getHoraInicio(), horario.getHoraFin());
    }

    /**
     * Muestra el formulario de edición de una reserva.
     *
     * @param reservaId ID de la reserva a editar.
     * @param model Modelo para pasar datos a la vista.
     * @param redirectAttributes Atributos para redirección con mensajes flash.
     * @return Nombre de la vista a mostrar.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Integer reservaId, Model model, RedirectAttributes redirectAttributes) {
        try {
            String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
            if (errorUsuario != null) return errorUsuario;
            
            Usuario usuario = sessionData.getUsuario();
            
            Reserva reserva = reservaService.findById(reservaId);
            if (reserva == null) {
                redirectAttributes.addFlashAttribute(ERROR, RESERVA_NO_ENCONTRADA);
                return REDIRECT_MIS_RESERVAS;
            }
            
            log.info("Reserva cargada para edición - ID: {}, tiene convocatoria: {}", 
                    reservaId, reserva.getConvocatoria() != null);
            if (reserva.getConvocatoria() != null) {
                log.info("Convocatoria - enlace: {}, observaciones: {}, convocados: {}", 
                        reserva.getConvocatoria().getEnlace(),
                        reserva.getConvocatoria().getObservaciones(),
                        reserva.getConvocatoria().getConvocados() != null ? reserva.getConvocatoria().getConvocados().size() : 0);
            }
            
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ERROR, "No tienes permisos para editar esta reserva.");
                return REDIRECT_MIS_RESERVAS;
            }
            
            if (reserva.getFechaReserva().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ERROR, "No se pueden editar reservas pasadas.");
                return REDIRECT_RESERVAS_ESTABLECIMIENTO + reserva.getEstablecimiento().getId();
            }
            
            model.addAttribute("reserva", reserva);
            model.addAttribute(ESTABLECIMIENTO, reserva.getEstablecimiento());
            model.addAttribute("isEdit", true);
			
			if (reserva.getConvocatoria() != null) {
				log.info("Agregando convocatoria al modelo para edición - enlace: {}, observaciones: {}", 
						reserva.getConvocatoria().getEnlace(), reserva.getConvocatoria().getObservaciones());
				model.addAttribute("convocatoria", reserva.getConvocatoria());
				
				if (reserva.getConvocatoria().getConvocados() != null && 
				    !reserva.getConvocatoria().getConvocados().isEmpty()) {
					List<UsuarioSimpleDTO> convocados = reserva.getConvocatoria().getConvocados().stream()
						.map(Convocado::getUsuario)
						.filter(Objects::nonNull)
						.map(usr -> new UsuarioSimpleDTO(usr.getId(), usr.getNombre(), usr.getApellidos(), usr.getCorreo()))
						.toList();
					log.info("Agregando {} convocados al modelo: {}", convocados.size(), 
							convocados.stream().map(UsuarioSimpleDTO::getNombre).toList());
					model.addAttribute("convocados", convocados);
				} else {
					log.info("Convocatoria sin convocados - solo enlace/observaciones");
					model.addAttribute("convocados", new ArrayList<>());
				}
			} else {
				log.info("No hay convocatoria asociada a la reserva");
			}
            
            List<FranjaHoraria> franjasActivas = obtenerFranjasActivas(reserva.getEstablecimiento());
            model.addAttribute("franjasHorarias", franjasActivas);
            
            SlotsData slotsData = generarSlotsData(reserva.getEstablecimiento(), franjasActivas);
            model.addAttribute("requiereSlotsPredefinidos", slotsData.isRequiereSlots());
            model.addAttribute("slotsDisponibles", slotsData.getSlotsDisponibles());
            
            if (slotsData.isRequiereSlots()) {
                String slotActual = calcularSlotActual(reserva);
                model.addAttribute("slotActual", slotActual);
            }
            
            return "reservas/editar_reserva";
            
        } catch (Exception e) {
            log.error("Error al cargar formulario de edición de reserva: ", e);
            redirectAttributes.addFlashAttribute(ERROR, "Error al cargar la reserva para edición.");
            return REDIRECT_MIS_RESERVAS;
        }
    }

    /**
     * Actualiza una reserva existente.
     *
     * @param reservaId ID de la reserva a actualizar.
     * @param fechaStr Fecha de la reserva como String.
     * @param horaInicioStr Hora de inicio como String.
     * @param horaFinStr Hora de fin como String.
     * @param slotSeleccionado Slot seleccionado como String.
     * @param enlaceReunion Enlace de reunión opcional.
     * @param observaciones Observaciones opcionales.
     * @param usuariosConvocados Array de IDs de usuarios convocados.
     * @param redirectAttributes Atributos para redirección con mensajes flash.
     * @return Redirección a la página correspondiente.
     */
    @PostMapping("/editar/{id}")
    public String actualizarReserva(@PathVariable("id") Integer reservaId,
                                   @RequestParam("fecha") String fechaStr,
                                   @RequestParam(value = "horaInicio", required = false) String horaInicioStr,
                                   @RequestParam(value = "horaFin", required = false) String horaFinStr,
                                   @RequestParam(value = "slotSeleccionado", required = false) String slotSeleccionado,
                                   @RequestParam(value = "enlaceReunion", required = false) String enlaceReunion,
                                   @RequestParam(value = "observaciones", required = false) String observaciones,
                                   @RequestParam(value = "usuariosConvocados", required = false) String[] usuariosConvocados,
                                   RedirectAttributes redirectAttributes) {
        try {
            String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
            if (errorUsuario != null) return errorUsuario;
            
            Usuario usuario = sessionData.getUsuario();
            
            Reserva reservaExistente = reservaService.findById(reservaId);
            if (reservaExistente == null) {
                redirectAttributes.addFlashAttribute(ERROR, RESERVA_NO_ENCONTRADA);
                return REDIRECT_MIS_RESERVAS;
            }
            
            if (!reservaExistente.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ERROR, "No tienes permisos para editar esta reserva.");
                return REDIRECT_MIS_RESERVAS;
            }
            
            if (reservaExistente.getFechaReserva().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ERROR, "No se pueden editar reservas pasadas.");
                return REDIRECT_RESERVAS_ESTABLECIMIENTO + reservaExistente.getEstablecimiento().getId();
            }
            
            Establecimiento establecimiento = reservaExistente.getEstablecimiento();
            
            HorarioReserva horario = parsearHorarios(fechaStr, horaInicioStr, horaFinStr, slotSeleccionado, redirectAttributes);
            if (horario == null) {
                return REDIRECT_MIS_RESERVAS_EDITAR + reservaId;
            }
            
            String errorFranja = validarFranjaHoraria(establecimiento, horario, redirectAttributes);
            if (errorFranja != null) {
                return REDIRECT_MIS_RESERVAS_EDITAR + reservaId;
            }
            
            String errorAforo = validarDisponibilidadAforo(establecimiento, horario, reservaExistente, redirectAttributes);
            if (errorAforo != null) {
                return errorAforo;
            }
            
            reservaExistente.setFechaReserva(LocalDateTime.of(horario.getFecha(), horario.getHoraInicio()));
            reservaExistente.setHoraFin(horario.getHoraFin());
            
            // IMPORTANTE: Determinar si hay datos de convocatoria
			boolean tieneConvocatoria = StringUtils.isNotBlank(enlaceReunion) || StringUtils.isNotBlank(observaciones)
					|| (usuariosConvocados != null && usuariosConvocados.length > 0);
            
            reservaExistente.setConvocatoria(null);
            
            convocatoriaService.deleteByReserva(reservaExistente);
            
            Reserva reservaActualizada = reservaService.save(reservaExistente);
            
            if (tieneConvocatoria) {
                Convocatoria convocatoria = new Convocatoria();
                convocatoria.setReserva(reservaActualizada);
                convocatoria.setEnlace(enlaceReunion);
                convocatoria.setObservaciones(observaciones);
                reservaActualizada.setConvocatoria(convocatoria);
                
                gestionarConvocatorias(reservaActualizada, usuariosConvocados);
                reservaActualizada.setConvocatoria(convocatoriaService.save(convocatoria));
            }

            emailService.enviarNotificacionReservaModificada(reservaActualizada);

            redirectAttributes.addFlashAttribute(EXITO, "Reserva actualizada correctamente.");
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimiento.getId();
            
        } catch (Exception e) {
            log.error("Error al actualizar reserva: ", e);
            redirectAttributes.addFlashAttribute(ERROR, "Error al actualizar la reserva: " + e.getMessage());
            return REDIRECT_MIS_RESERVAS;
        }
    }

    /**
     * Anula una reserva existente.
     * Envía notificaciones por correo al usuario y convocados antes de eliminar la reserva.
     * 
     * @param reservaId ID de la reserva a anular
     * @param redirectAttributes Para mensajes de redirección
     * @return Redirección a la página del establecimiento
     */
    @PostMapping("/anular/{id}")
    public String anularReserva(@PathVariable("id") Integer reservaId, RedirectAttributes redirectAttributes) {
        try {
            String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
            if (errorUsuario != null) return errorUsuario;
            
            Usuario usuario = sessionData.getUsuario();
            
            Reserva reserva = reservaService.findById(reservaId);
            if (reserva == null) {
                redirectAttributes.addFlashAttribute(ERROR, RESERVA_NO_ENCONTRADA);
                return REDIRECT_MIS_RESERVAS;
            }
            
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ERROR, "No tiene permisos para anular esta reserva.");
                return REDIRECT_MIS_RESERVAS;
            }
            
            if (reserva.getFechaReserva().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ERROR, "No se pueden anular reservas pasadas.");
                return String.format("redirect:/misreservas/establecimiento/%d", reserva.getEstablecimiento().getId());
            }
            
            Establecimiento establecimiento = reserva.getEstablecimiento();
            List<String> correosNotificacion = new ArrayList<>();
            
            correosNotificacion.add(usuario.getCorreo());
            
            if (reserva.getConvocatoria() != null && reserva.getConvocatoria().getConvocados() != null) {
                reserva.getConvocatoria().getConvocados().forEach(convocado -> {
                    if (convocado.getUsuario() != null && convocado.getUsuario().getCorreo() != null) {
                        correosNotificacion.add(convocado.getUsuario().getCorreo());
                    }
                });
            }
            
            notificarAnulacion(reservaId, reserva, correosNotificacion);
            
            reservaService.delete(reserva);
            
            log.info("Reserva {} anulada exitosamente por usuario {}", reservaId, usuario.getId());
            redirectAttributes.addFlashAttribute(EXITO, "Reserva anulada exitosamente. Se han enviado notificaciones por correo.");
            
            return String.format("redirect:/misreservas/establecimiento/%d", establecimiento.getId());
            
        } catch (Exception e) {
            log.error("Error al anular reserva {}: {}", reservaId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute(ERROR, "Error al anular la reserva: " + e.getMessage());
            return REDIRECT_MIS_RESERVAS;
        }
    }

    /**
	 * Envía notificaciones por correo electrónico al usuario y convocados sobre la anulación de la reserva.
	 * 
	 * @param reservaId ID de la reserva anulada
	 * @param reserva Reserva que se ha anulado
	 * @param correosNotificacion Lista de correos electrónicos a los que enviar la notificación
	 */
	private void notificarAnulacion(Integer reservaId, Reserva reserva, List<String> correosNotificacion) {
		try {
		    emailService.enviarNotificacionAnulacion(reserva, correosNotificacion);
		} catch (Exception e) {
		    log.warn("Error al enviar notificaciones de anulación para reserva {}: {}", reservaId, e.getMessage());
		}
	}

    /**
     * Prepara todos los datos necesarios para mostrar el calendario de reservas.
     * 
     * @param establecimiento
     * @param model
     */
    private void prepararDatosCalendario(Establecimiento establecimiento, Model model) {
        Usuario usuario = sessionData.getUsuario();
        
        List<FranjaHoraria> franjasActivas = obtenerFranjasActivas(establecimiento);
        
        ReservasUsuario reservasUsuario = obtenerReservasUsuario(usuario, establecimiento);
        
        SlotsData slotsData = generarSlotsData(establecimiento, franjasActivas);
        
        configurarModeloCalendario(model, establecimiento, franjasActivas, reservasUsuario, slotsData);
    }

    /**
     * Prepara los datos necesarios para mostrar el calendario mensual de disponibilidad.
     * 
     * @param establecimiento Establecimiento para el cual mostrar el calendario
     * @param mes Mes seleccionado (null para el mes actual)
     * @param anio Año seleccionado (null para el año actual)
     * @param model Modelo para pasar datos a la vista
     */
    private void prepararDatosCalendarioMensual(Establecimiento establecimiento, Integer mes, Integer anio, Model model) {
        try {
            LocalDate hoy = LocalDate.now();
            int mesActual = (mes != null) ? mes : hoy.getMonthValue();
            int anioActual = (anio != null) ? anio : hoy.getYear();
            
            log.info("Preparando calendario para establecimiento: {}, mes: {}, año: {}", establecimiento.getId(), mesActual, anioActual);
            
            YearMonth yearMonth = YearMonth.of(anioActual, mesActual);
            LocalDate primerDiaMes = yearMonth.atDay(1);
            LocalDate ultimoDiaMes = yearMonth.atEndOfMonth();
            
            List<Reserva> reservasMes = reservaService.findByEstablecimientoAndFechaReservaBetween(
                establecimiento, 
                primerDiaMes.atStartOfDay(), 
                ultimoDiaMes.plusDays(1).atStartOfDay()
            );
            
            log.info("Encontradas {} reservas para el mes {}/{}", reservasMes.size(), mesActual, anioActual);

            CalendarioData calendarioData = generarCalendarioConDisponibilidad(establecimiento, yearMonth, reservasMes);
            
            log.info("Calendario generado con {} semanas", calendarioData.getSemanas().size());
            
            model.addAttribute(ESTABLECIMIENTO, establecimiento);
            model.addAttribute("calendarioData", calendarioData);
            model.addAttribute("yearMonth", yearMonth);
            model.addAttribute("mesActual", mesActual);
            model.addAttribute("anioActual", anioActual);
            
            // Navegación entre meses
            YearMonth mesPrevio = yearMonth.minusMonths(1);
            YearMonth mesSiguiente = yearMonth.plusMonths(1);
            model.addAttribute("mesPrevio", mesPrevio.getMonthValue());
            model.addAttribute("anioPrevio", mesPrevio.getYear());
            model.addAttribute("mesSiguiente", mesSiguiente.getMonthValue());
            model.addAttribute("anioSiguiente", mesSiguiente.getYear());
            
            log.info("Datos del calendario mensual preparados exitosamente");
        } catch (Exception e) {
            log.error("Error al preparar datos del calendario mensual: {}", e.getMessage(), e);
			model.addAttribute(ERROR, "Error al cargar el calendario mensual.");
		}
    }

    /**
     * Genera los datos del calendario mensual con información de disponibilidad.
     * 
     * @param establecimiento Establecimiento para el cual generar el calendario
     * @param yearMonth Mes y año del calendario
     * @param reservasMes Lista de reservas del mes
     * @return Datos del calendario con disponibilidad
     */
	private CalendarioData generarCalendarioConDisponibilidad(Establecimiento establecimiento, YearMonth yearMonth, List<Reserva> reservasMes) {
		RangoCalendario rango = calcularRangoCalendario(yearMonth);
		List<List<DiaCalendario>> semanas = generarSemanas(establecimiento, yearMonth, reservasMes, rango);
		String nombreMes = obtenerNombreMes(yearMonth.getMonthValue());

		return new CalendarioData(semanas, nombreMes, yearMonth.getYear(), yearMonth.getMonthValue());
	}

	/**
	 * Calcula el rango de fechas a mostrar en el calendario, incluyendo días del mes anterior y siguiente para completar semanas.
	 * 
	 * @param yearMonth Mes y año del calendario
	 * @return Rango de fechas para el calendario
	 */
	private RangoCalendario calcularRangoCalendario(YearMonth yearMonth) {
		LocalDate primerDiaMes = yearMonth.atDay(1);
		LocalDate ultimoDiaMes = yearMonth.atEndOfMonth();

		LocalDate primerDiaCalendario = encontrarPrimerLunesAnterior(primerDiaMes);
		LocalDate ultimoDiaCalendario = encontrarUltimoDomingoPosterior(ultimoDiaMes);

		return new RangoCalendario(primerDiaCalendario, ultimoDiaCalendario);
	}

	/**
	 * Encuentra el primer lunes anterior o igual a la fecha dada.
	 * 
	 * @param fecha Fecha de referencia
	 * @return Primer lunes anterior o igual a la fecha
	 */
	private LocalDate encontrarPrimerLunesAnterior(LocalDate fecha) {
		LocalDate resultado = fecha;
		while (resultado.getDayOfWeek() != DayOfWeek.MONDAY) {
			resultado = resultado.minusDays(1);
		}
		return resultado;
	}

	/**
	 * Encuentra el último domingo posterior o igual a la fecha dada.
	 * 
	 * @param fecha Fecha de referencia
	 * @return Último domingo posterior o igual a la fecha
	 */
	private LocalDate encontrarUltimoDomingoPosterior(LocalDate fecha) {
		LocalDate resultado = fecha;
		while (resultado.getDayOfWeek() != DayOfWeek.SUNDAY) {
			resultado = resultado.plusDays(1);
		}
		return resultado;
	}

	/**
	 * Obtiene el nombre del mes en español.
	 * 
	 * @param mes Número del mes (1-12)
	 * @return Nombre del mes en español
	 */
	private List<List<DiaCalendario>> generarSemanas(Establecimiento establecimiento, YearMonth yearMonth,
			List<Reserva> reservasMes, RangoCalendario rango) {
		List<List<DiaCalendario>> semanas = new ArrayList<>();
		LocalDate fechaActual = rango.getFechaInicio();

		while (!fechaActual.isAfter(rango.getFechaFin())) {
			List<DiaCalendario> semana = generarSemana(establecimiento, yearMonth, reservasMes, fechaActual);
			semanas.add(semana);
			fechaActual = fechaActual.plusWeeks(1);
		}

		return semanas;
	}

	/**
	 * Genera una semana del calendario con información de disponibilidad para cada día.
	 * 
	 * @param establecimiento Establecimiento para el cual generar la semana
	 * @param yearMonth Mes y año del calendario
	 * @param reservasMes Lista de reservas del mes
	 * @param inicioSemana Fecha de inicio de la semana (lunes)
	 * @return Lista de días con información de disponibilidad para la semana
	 */
	private List<DiaCalendario> generarSemana(Establecimiento establecimiento, YearMonth yearMonth,
			List<Reserva> reservasMes, LocalDate inicioSemana) {
		List<DiaCalendario> semana = new ArrayList<>();
		LocalDate fechaDia = inicioSemana;

		for (int i = 0; i < 7; i++) {
			DiaCalendario dia = crearDiaCalendario(establecimiento, yearMonth, reservasMes, fechaDia);
			semana.add(dia);
			fechaDia = fechaDia.plusDays(1);
		}

		return semana;
	}

	/**
	 * Crea un objeto DiaCalendario con información de disponibilidad para un día específico.
	 * 
	 * @param establecimiento Establecimiento para el cual generar el día
	 * @param yearMonth Mes y año del calendario
	 * @param reservasMes Lista de reservas del mes
	 * @param fecha Fecha del día a crear
	 * @return Objeto DiaCalendario con información de disponibilidad
	 */
	private DiaCalendario crearDiaCalendario(Establecimiento establecimiento, YearMonth yearMonth,
			List<Reserva> reservasMes, LocalDate fecha) {
		boolean esDelMesActual = perteneceAlMesActual(fecha, yearMonth);
		boolean tieneDisponibilidad = false;
		String resumen = "";

		if (esDiaValidoParaReserva(fecha, esDelMesActual)) {
			DisponibilidadDia disponibilidad = calcularDisponibilidadDia(establecimiento, fecha, reservasMes);
			tieneDisponibilidad = disponibilidad.tieneDisponibilidad();
			resumen = disponibilidad.getResumen();
		}

		return new DiaCalendario(fecha, esDelMesActual, tieneDisponibilidad, resumen);
	}

	/**
	 * Verifica si una fecha pertenece al mes y año especificados.
	 * 
	 * @param fecha Fecha a verificar
	 * @param yearMonth Mes y año de referencia
	 * @return true si la fecha pertenece al mes y año, false en caso contrario
	 */
	private boolean perteneceAlMesActual(LocalDate fecha, YearMonth yearMonth) {
		return fecha.getMonth() == yearMonth.getMonth() && fecha.getYear() == yearMonth.getYear();
	}

	/**
	 * Verifica si un día es válido para realizar reservas (dentro del mes actual y no en el pasado).
	 * 
	 * @param fecha Fecha a verificar
	 * @param esDelMesActual true si la fecha pertenece al mes actual
	 * @return true si el día es válido para reservas, false en caso contrario
	 */
	private boolean esDiaValidoParaReserva(LocalDate fecha, boolean esDelMesActual) {
		return esDelMesActual && !fecha.isBefore(LocalDate.now());
	}

	/**
	 * Calcula la disponibilidad y resumen de reservas para un día específico.
	 * 
	 * @param establecimiento Establecimiento a verificar
	 * @param fecha           Fecha del día a verificar
	 * @param reservasMes     Lista de reservas del mes
	 * @return Objeto DisponibilidadDia con información de disponibilidad y resumen
	 */
	private DisponibilidadDia calcularDisponibilidadDia(Establecimiento establecimiento, LocalDate fecha, List<Reserva> reservasMes) {
		long reservasDelDia = contarReservasDelDia(reservasMes, fecha);
		boolean tieneDisponibilidad = verificarDisponibilidadDia(establecimiento, fecha, reservasMes);
		String resumen = generarResumenReservas(reservasDelDia);

		return new DisponibilidadDia(tieneDisponibilidad, resumen);
	}

	/**
	 * Cuenta el número de reservas en un día específico.
	 * 
	 * @param reservasMes Lista de reservas del mes
	 * @param fecha Fecha del día a contar
	 * @return Número de reservas en el día
	 */
	private long contarReservasDelDia(List<Reserva> reservasMes, LocalDate fecha) {
		return reservasMes.stream().filter(reserva -> reserva.getFechaReserva().toLocalDate().equals(fecha)).count();
	}

	/**
	 * Genera un resumen textual del número de reservas en un día.
	 * 
	 * @param reservasDelDia Número de reservas en el día
	 * @return Resumen textual (e.g., "3 reservas" o "1 reserva"), o cadena vacía si
	 *         no hay reservas
	 */
	private String generarResumenReservas(long reservasDelDia) {
		if (reservasDelDia > 0) {
			return reservasDelDia + " reserva" + (reservasDelDia > 1 ? "s" : "");
		}
		return "";
	}

	// Clases auxiliares para mejorar la legibilidad y encapsulación

	/** Clase que representa un día en el calendario con su disponibilidad */
	private static class RangoCalendario {
		private final LocalDate fechaInicio;
		private final LocalDate fechaFin;

		public RangoCalendario(LocalDate fechaInicio, LocalDate fechaFin) {
			this.fechaInicio = fechaInicio;
			this.fechaFin = fechaFin;
		}

		public LocalDate getFechaInicio() {
			return fechaInicio;
		}

		public LocalDate getFechaFin() {
			return fechaFin;
		}
	}

	/** Clase que representa la disponibilidad de un día específico */
	private static class DisponibilidadDia {
		private final boolean tieneDisponibilidad;
		private final String resumen;

		public DisponibilidadDia(boolean tieneDisponibilidad, String resumen) {
			this.tieneDisponibilidad = tieneDisponibilidad;
			this.resumen = resumen;
		}

		public boolean tieneDisponibilidad() {
			return tieneDisponibilidad;
		}

		public String getResumen() {
			return resumen;
		}
	}

    /**
     * Verifica si hay disponibilidad para reservar en un día específico.
     * 
     * @param establecimiento Establecimiento a verificar
     * @param fecha Fecha a verificar
     * @param reservasMes Lista de todas las reservas del mes
     * @return true si hay disponibilidad, false en caso contrario
     */
    private boolean verificarDisponibilidadDia(Establecimiento establecimiento, LocalDate fecha, List<Reserva> reservasMes) {
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        
        List<FranjaHoraria> franjasDelDia = establecimiento.getFranjasHorarias().stream()
            .filter(franja -> franja.getDiaSemana().equals(diaSemana))
            .toList();
        
        if (franjasDelDia.isEmpty()) {
            log.debug("Establecimiento {} cerrado el día {}", establecimiento.getId(), fecha);
            return false;
        }
        
        List<Reserva> reservasDelDia = reservasMes.stream()
            .filter(reserva -> reserva.getFechaReserva().toLocalDate().equals(fecha))
            .toList();
        
        log.debug("Verificando disponibilidad para {} el {}. Reservas del día: {}, Aforo: {}", 
                establecimiento.getNombre(), fecha, reservasDelDia.size(), establecimiento.getAforo());
        
        if (establecimiento.getAforo() == null || establecimiento.getAforo() <= 0) {
            log.debug("Establecimiento sin aforo limitado - siempre disponible");
            return true;
        }
        
        for (FranjaHoraria franja : franjasDelDia) {
            boolean disponibleEnFranja = tieneDisponibilidadEnFranja(establecimiento, franja, reservasDelDia);
            log.debug("Franja {}-{}: disponible = {}", franja.getHoraInicio(), franja.getHoraFin(), disponibleEnFranja);
            if (disponibleEnFranja) {
                return true;
            }
        }
        
        log.debug("No hay disponibilidad en ninguna franja para el día {}", fecha);
        return false;
    }
    
    /**
     * Verifica si hay disponibilidad en una franja horaria específica.
     * 
     * @param establecimiento Establecimiento a verificar
     * @param franja Franja horaria a verificar
     * @param reservasDelDia Lista de reservas del día
     * @return true si hay disponibilidad en la franja, false en caso contrario
     */
    private boolean tieneDisponibilidadEnFranja(Establecimiento establecimiento, FranjaHoraria franja, List<Reserva> reservasDelDia) {
        LocalTime inicioFranja = franja.getHoraInicio();
        LocalTime finFranja = franja.getHoraFin();
        
        LocalTime horaActual = inicioFranja;
        while (horaActual.isBefore(finFranja)) {
            final LocalTime inicioSlot = horaActual;
            final LocalTime finSlot = horaActual.plusMinutes(30).isAfter(finFranja) ? 
                                     finFranja : horaActual.plusMinutes(30);
            
            long reservasSolapadas = reservasDelDia.stream()
                .filter(reserva -> {
                    LocalTime inicioReserva = reserva.getFechaReserva().toLocalTime();
                    LocalTime finReserva = reserva.getHoraFin();
                    
                    return !(finReserva.isBefore(inicioSlot) || inicioReserva.isAfter(finSlot) || 
                            finReserva.equals(inicioSlot) || inicioReserva.equals(finSlot));
                })
                .count();
            
            if (reservasSolapadas < establecimiento.getAforo()) {
                return true;
            }
            
            horaActual = horaActual.plusMinutes(30);
        }
        
        return false;
    }

    /**
     * Obtiene el nombre del mes en español.
     * 
     * @param numeroMes Número del mes (1-12)
     * @return Nombre del mes en español
     */
    private String obtenerNombreMes(int numeroMes) {
        String[] meses = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return meses[numeroMes - 1];
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
     * OPTIMIZADO: Limita el número de reservas pasadas para evitar cargar demasiados datos
     * 
     * @param establecimiento
     */
    private ReservasUsuario obtenerReservasUsuario(Usuario usuario, Establecimiento establecimiento) {
        LocalDateTime fechaActual = LocalDateTime.now();
        
        List<Reserva> reservasPasadas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
        List<Reserva> reservasFuturas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
        
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
     * Calcula el slot actual de una reserva basado en su hora de inicio y fin.
     * 
     * @param reserva La reserva de la cual calcular el slot
     * @return El slot en formato "HH:mm - HH:mm" o null si no se puede determinar
     */
    private String calcularSlotActual(Reserva reserva) {
        if (reserva == null || reserva.getFechaReserva() == null) {
            return null;
        }
        
        LocalTime horaInicio = reserva.getFechaReserva().toLocalTime();
        LocalTime horaFin = reserva.getHoraFin();
        
        if (horaFin == null) {
            return null;
        }
        
        // Formatear como "HH:mm - HH:mm"
         return String.format("%s - %s", 
             horaInicio.format(DateTimeFormatter.ofPattern(FORMATO_HORAS)),
             horaFin.format(DateTimeFormatter.ofPattern(FORMATO_HORAS)));
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
        model.addAttribute(ESTABLECIMIENTO, establecimiento);
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

    /**
     * Endpoint AJAX para obtener reservas paginadas para scroll infinito.
     * 
     * @param establecimientoId ID del establecimiento
     * @param paginaFuturas Página actual de reservas futuras
     * @param paginaPasadas Página actual de reservas pasadas
     * @param tamanyoPagina Tamaño de página
     * @return ResponseEntity con las reservas paginadas
     */
    @GetMapping("/reservas-paginadas")
    @ResponseBody
    public ResponseEntity<ReservasPaginadasResponse> obtenerReservasPaginadas(
            @RequestParam Integer establecimientoId,
            @RequestParam(defaultValue = "1") int paginaFuturas,
            @RequestParam(defaultValue = "1") int paginaPasadas,
            @RequestParam(defaultValue = "5") int tamanyoPagina) {
        
        try {
            Usuario usuario = sessionData.getUsuario();
            if (usuario == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
            if (establecimientoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Establecimiento establecimiento = establecimientoOpt.get();
            
            if (!usuarioService.establecimientoAsignado(usuario, establecimiento)) {
                return ResponseEntity.status(403).build();
            }

            LocalDateTime fechaActual = LocalDateTime.now();
            
            List<Reserva> todasReservasFuturas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
            todasReservasFuturas.sort(Comparator.comparing(Reserva::getFechaReserva));
            
            int inicioFuturas = (paginaFuturas - 1) * tamanyoPagina;
            int finFuturas = Math.min(inicioFuturas + tamanyoPagina, todasReservasFuturas.size());
            List<Reserva> reservasFuturas = inicioFuturas < todasReservasFuturas.size() ? todasReservasFuturas.subList(inicioFuturas, finFuturas) : new ArrayList<>();
            boolean hayMasReservasFuturas = finFuturas < todasReservasFuturas.size();
            
            List<Reserva> todasReservasPasadas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
            todasReservasPasadas.sort(Comparator.comparing(Reserva::getFechaReserva).reversed());
            
            int inicioPasadas = (paginaPasadas - 1) * tamanyoPagina;
            int finPasadas = Math.min(inicioPasadas + tamanyoPagina, todasReservasPasadas.size());
            List<Reserva> reservasPasadas = inicioPasadas < todasReservasPasadas.size() ? todasReservasPasadas.subList(inicioPasadas, finPasadas) : new ArrayList<>();
            boolean hayMasReservasPasadas = finPasadas < todasReservasPasadas.size();
            
            // Convertir a DTOs
            List<ReservaDTO> reservasFuturasDTO = reservasFuturas.stream().map(this::convertirAReservaDTO).toList();
            List<ReservaDTO> reservasPasadasDTO = reservasPasadas.stream().map(this::convertirAReservaDTO).toList();

            ReservasPaginadasResponse response = new ReservasPaginadasResponse();
            response.setReservasFuturas(reservasFuturasDTO);
            response.setReservasPasadas(reservasPasadasDTO);
            response.setHayMasReservasFuturas(hayMasReservasFuturas);
            response.setHayMasReservasPasadas(hayMasReservasPasadas);
            response.setPaginaFuturas(paginaFuturas);
            response.setPaginaPasadas(paginaPasadas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al obtener reservas paginadas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Convierte una Reserva a ReservaDTO.
     */
    private ReservaDTO convertirAReservaDTO(Reserva reserva) {
        ReservaDTO dto = new ReservaDTO();
        dto.setId(reserva.getId());
        dto.setFechaReserva(reserva.getFechaReserva());
        dto.setHoraFin(reserva.getHoraFin() != null ? reserva.getHoraFin().toString() : null);
        
        if (reserva.getConvocatoria() != null) {
            ConvocatoriaDTO convocatoriaDTO = new ConvocatoriaDTO();
            convocatoriaDTO.setEnlace(reserva.getConvocatoria().getEnlace());
            convocatoriaDTO.setObservaciones(reserva.getConvocatoria().getObservaciones());
            
            if (reserva.getConvocatoria().getConvocados() != null) {
                List<ConvocadoDTO> convocadosDTO = reserva.getConvocatoria().getConvocados().stream()
                    .map(convocado -> {
                        ConvocadoDTO convocadoDTO = new ConvocadoDTO();
                        UsuarioSimpleDTO usuarioDTO = new UsuarioSimpleDTO(
                            convocado.getUsuario().getId(),
                            convocado.getUsuario().getNombre(),
                            convocado.getUsuario().getApellidos(),
                            convocado.getUsuario().getCorreo()
                        );
                        convocadoDTO.setUsuario(usuarioDTO);
                        return convocadoDTO;
                    }).toList();
                convocatoriaDTO.setConvocados(convocadosDTO);
            }
            
            dto.setConvocatoria(convocatoriaDTO);
        }
        
        return dto;
    }

    /**
     * DTO para transferir datos de usuario en búsquedas AJAX.
     */
    public static class UsuarioDTO {
        private String id;
        private String nombre;
        private String correo;

        public UsuarioDTO(String id, String nombre, String correo) {
            this.id = id;
            this.nombre = nombre;
            this.correo = correo;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
    }
    
    /**
     * DTO simplificado para transferir datos de usuario sin relaciones complejas.
     */
    public static class UsuarioSimpleDTO {
        private String id;
        private String nombre;
        private String apellidos;
        private String correo;

        public UsuarioSimpleDTO(String id, String nombre, String apellidos, String correo) {
            this.id = id;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.correo = correo;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getApellidos() { return apellidos; }
        public String getCorreo() { return correo; }
    }

    /**
     * DTO para respuesta de slots disponibles.
     */
    public static class SlotsDisponiblesResponse {
        private List<SlotReservaUtil.SlotTiempo> slots;
        private Integer aforo;
        private Boolean tieneAforo;

        public List<SlotReservaUtil.SlotTiempo> getSlots() {
            return slots;
        }

        public void setSlots(List<SlotReservaUtil.SlotTiempo> slots) {
            this.slots = slots;
        }

        public Integer getAforo() {
            return aforo;
        }

        public void setAforo(Integer aforo) {
            this.aforo = aforo;
        }

        public Boolean getTieneAforo() {
            return tieneAforo;
        }

        public void setTieneAforo(Boolean tieneAforo) {
            this.tieneAforo = tieneAforo;
        }
    }

    /**
     * DTO para respuesta de franjas disponibles.
     */
    public static class FranjasDisponiblesResponse {
        private List<ReservaService.FranjaDisponibilidad> franjas;
        private Integer aforo;
        private Boolean tieneAforo;

        public List<ReservaService.FranjaDisponibilidad> getFranjas() {
            return franjas;
        }

        public void setFranjas(List<ReservaService.FranjaDisponibilidad> franjas) {
            this.franjas = franjas;
        }

        public Integer getAforo() {
            return aforo;
        }

        public void setAforo(Integer aforo) {
            this.aforo = aforo;
        }

        public Boolean getTieneAforo() {
            return tieneAforo;
        }

        public void setTieneAforo(Boolean tieneAforo) {
            this.tieneAforo = tieneAforo;
        }
    }
    
    /**
     * DTO para respuesta de reservas paginadas.
     */
    public static class ReservasPaginadasResponse {
        private List<ReservaDTO> reservasFuturas;
        private List<ReservaDTO> reservasPasadas;
        private boolean hayMasReservasFuturas;
        private boolean hayMasReservasPasadas;
        private int paginaFuturas;
        private int paginaPasadas;
        
        public List<ReservaDTO> getReservasFuturas() { return reservasFuturas; }
        public void setReservasFuturas(List<ReservaDTO> reservasFuturas) { this.reservasFuturas = reservasFuturas; }
        
        public List<ReservaDTO> getReservasPasadas() { return reservasPasadas; }
        public void setReservasPasadas(List<ReservaDTO> reservasPasadas) { this.reservasPasadas = reservasPasadas; }
        
        public boolean isHayMasReservasFuturas() { return hayMasReservasFuturas; }
        public void setHayMasReservasFuturas(boolean hayMasReservasFuturas) { this.hayMasReservasFuturas = hayMasReservasFuturas; }
        
        public boolean isHayMasReservasPasadas() { return hayMasReservasPasadas; }
        public void setHayMasReservasPasadas(boolean hayMasReservasPasadas) { this.hayMasReservasPasadas = hayMasReservasPasadas; }
        
        public int getPaginaFuturas() { return paginaFuturas; }
        public void setPaginaFuturas(int paginaFuturas) { this.paginaFuturas = paginaFuturas; }
        
        public int getPaginaPasadas() { return paginaPasadas; }
        public void setPaginaPasadas(int paginaPasadas) { this.paginaPasadas = paginaPasadas; }
    }
    
    /**
     * DTO para representar una reserva.
     */
    public static class ReservaDTO {
        private Integer id;
        private LocalDateTime fechaReserva;
        private String horaFin;
        private ConvocatoriaDTO convocatoria;
        
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public LocalDateTime getFechaReserva() { return fechaReserva; }
        public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
        
        public String getHoraFin() { return horaFin; }
        public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
        
        public ConvocatoriaDTO getConvocatoria() { return convocatoria; }
        public void setConvocatoria(ConvocatoriaDTO convocatoria) { this.convocatoria = convocatoria; }
    }
    
    /**
     * DTO para representar una convocatoria.
     */
    public static class ConvocatoriaDTO {
        private String enlace;
        private String observaciones;
        private List<ConvocadoDTO> convocados;
        
        public String getEnlace() { return enlace; }
        public void setEnlace(String enlace) { this.enlace = enlace; }
        
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
        
        public List<ConvocadoDTO> getConvocados() { return convocados; }
        public void setConvocados(List<ConvocadoDTO> convocados) { this.convocados = convocados; }
    }
    
    /**
     * DTO para representar un convocado.
     */
    public static class ConvocadoDTO {
        private UsuarioSimpleDTO usuario;
        
        public UsuarioSimpleDTO getUsuario() { return usuario; }
        public void setUsuario(UsuarioSimpleDTO usuario) { this.usuario = usuario; }
    }
    
    /**
     * Clase auxiliar para encapsular los datos del calendario mensual.
     */
    public static class CalendarioData {
        private final List<List<DiaCalendario>> semanas;
        private final String nombreMes;
        private final int anyo;
        private final int mes;

        public CalendarioData(List<List<DiaCalendario>> semanas, String nombreMes, int anyo, int mes) {
            this.semanas = semanas;
            this.nombreMes = nombreMes;
            this.anyo = anyo;
            this.mes = mes;
        }

        public List<List<DiaCalendario>> getSemanas() { return semanas; }
        public String getNombreMes() { return nombreMes; }
        public int getAnyo() { return anyo; }
        public int getMes() { return mes; }
    }

    /**
     * Clase auxiliar para representar un día en el calendario.
     */
    public static class DiaCalendario {
        private final LocalDate fecha;
        private final boolean esDelMesActual;
        private final boolean tieneDisponibilidad;
        private final String resumen;

        public DiaCalendario(LocalDate fecha, boolean esDelMesActual, boolean tieneDisponibilidad, String resumen) {
            this.fecha = fecha;
            this.esDelMesActual = esDelMesActual;
            this.tieneDisponibilidad = tieneDisponibilidad;
            this.resumen = resumen;
        }

        public LocalDate getFecha() { return fecha; }
        public String getResumen() { return resumen; }
        public int getDia() { return fecha.getDayOfMonth(); }
        public boolean getEsHoy() { return fecha.equals(LocalDate.now()); }
        public boolean getEsPasado() { return fecha.isBefore(LocalDate.now()); }
        
        public boolean isEsDelMesActual() { return esDelMesActual; }
        public boolean isTieneDisponibilidad() { return tieneDisponibilidad; }
        public boolean esHoy() { return fecha.equals(LocalDate.now()); }
        public boolean esPasado() { return fecha.isBefore(LocalDate.now()); }
        
        public String getFechaIso() { return fecha.toString(); }
    }
    
    /**
     * Obtiene los períodos libres para un establecimiento en una fecha específica.
     * Solo se usa para establecimientos sin duración fija.
     * 
     * @param establecimiento Establecimiento a verificar
     * @param fecha Fecha para la cual obtener períodos libres
     * @return Lista de períodos libres
     */
    private List<PeriodoLibre> obtenerPeriodosLibres(Establecimiento establecimiento, LocalDate fecha) {
        List<PeriodoLibre> periodosLibres = new ArrayList<>();
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        
        List<FranjaHoraria> franjasDelDia = establecimiento.getFranjasHorarias().stream()
            .filter(franja -> franja.getDiaSemana().equals(diaSemana))
            .toList();
        
        if (franjasDelDia.isEmpty()) {
            return periodosLibres;
        }
        
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
        List<Reserva> reservasDelDia = reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento, inicioDelDia, finDelDia);
        
        for (FranjaHoraria franja : franjasDelDia) {
            List<PeriodoLibre> periodosEnFranja = calcularPeriodosLibresEnFranja(establecimiento, franja, reservasDelDia);
            periodosLibres.addAll(periodosEnFranja);
        }
        
        periodosLibres.sort(Comparator.comparing(PeriodoLibre::getHoraInicio));
        
        return periodosLibres;
    }
    
    /**
     * Calcula los períodos libres dentro de una franja horaria específica.
     * 
     * @param establecimiento Establecimiento
     * @param franja Franja horaria
     * @param reservasDelDia Reservas del día
     * @return Lista de períodos libres en la franja
     */
    private List<PeriodoLibre> calcularPeriodosLibresEnFranja(Establecimiento establecimiento, 
                                                            FranjaHoraria franja, 
                                                            List<Reserva> reservasDelDia) {
        List<PeriodoLibre> periodosLibres = new ArrayList<>();
        
        List<Reserva> reservasEnFranja = reservasDelDia.stream()
            .filter(reserva -> {
                LocalTime inicioReserva = reserva.getFechaReserva().toLocalTime();
                LocalTime finReserva = reserva.getHoraFin();
                
                return !(finReserva.isBefore(franja.getHoraInicio()) || 
                        inicioReserva.isAfter(franja.getHoraFin()) ||
                        finReserva.equals(franja.getHoraInicio()) || 
                        inicioReserva.equals(franja.getHoraFin()));
            })
            .sorted(Comparator.comparing(reserva -> reserva.getFechaReserva().toLocalTime()))
            .toList();
        
        LocalTime horaActual = franja.getHoraInicio();
        
        for (Reserva reserva : reservasEnFranja) {
            LocalTime inicioReserva = reserva.getFechaReserva().toLocalTime();
            
            if (horaActual.isBefore(inicioReserva)) {
                int espaciosLibres = calcularEspaciosDisponibles(establecimiento, horaActual, inicioReserva, reservasEnFranja);
                if (espaciosLibres > 0) {
                    periodosLibres.add(new PeriodoLibre(horaActual, inicioReserva, espaciosLibres));
                }
            }
            
            horaActual = reserva.getHoraFin().isAfter(horaActual) ? reserva.getHoraFin() : horaActual;
        }
        
        if (horaActual.isBefore(franja.getHoraFin())) {
            int espaciosLibres = calcularEspaciosDisponibles(establecimiento, horaActual, franja.getHoraFin(), reservasEnFranja);
            if (espaciosLibres > 0) {
                periodosLibres.add(new PeriodoLibre(horaActual, franja.getHoraFin(), espaciosLibres));
            }
        }
        
        return periodosLibres;
    }
    
    /**
     * Calcula cuántos espacios están disponibles en un período específico.
     * 
     * @param establecimiento Establecimiento
     * @param inicio Hora de inicio del período
     * @param fin Hora de fin del período
     * @param reservasEnFranja Reservas en la franja
     * @return Número de espacios disponibles
     */
    private int calcularEspaciosDisponibles(Establecimiento establecimiento, LocalTime inicio, 
                                           LocalTime fin, List<Reserva> reservasEnFranja) {
        if (establecimiento.getAforo() == null || establecimiento.getAforo() <= 0) {
            return 999;
        }
        
        long reservasSolapadas = reservasEnFranja.stream()
            .filter(reserva -> {
                LocalTime inicioReserva = reserva.getFechaReserva().toLocalTime();
                LocalTime finReserva = reserva.getHoraFin();
                
                return !(finReserva.isBefore(inicio) || inicioReserva.isAfter(fin) ||
                        finReserva.equals(inicio) || inicioReserva.equals(fin));
            })
            .count();
        
        return Math.max(0, establecimiento.getAforo() - (int)reservasSolapadas);
    }
    
    /**
     * Clase para representar un período libre en el establecimiento.
     */
    public static class PeriodoLibre {
        private final LocalTime horaInicio;
        private final LocalTime horaFin;
        private final int espaciosDisponibles;
        
        public PeriodoLibre(LocalTime horaInicio, LocalTime horaFin, int espaciosDisponibles) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.espaciosDisponibles = espaciosDisponibles;
        }
        
        public LocalTime getHoraInicio() { return horaInicio; }
        public LocalTime getHoraFin() { return horaFin; }
        public int getEspaciosDisponibles() { return espaciosDisponibles; }
        
        public String getHoraInicioFormateada() {
            return horaInicio.format(DateTimeFormatter.ofPattern(FORMATO_HORAS));
        }
        
        public String getHoraFinFormateada() {
            return horaFin.format(DateTimeFormatter.ofPattern(FORMATO_HORAS));
        }
        
        public String getDuracionFormateada() {
            Duration duracion = Duration.between(horaInicio, horaFin);
            long horas = duracion.toHours();
            long minutos = duracion.toMinutes() % 60;
            
            if (horas > 0) {
                return String.format("%dh %02dmin", horas, minutos);
            } else {
                return String.format("%d min", minutos);
            }
        }
        
        public boolean isIlimitado() {
            return espaciosDisponibles >= 999;
        }
    }
}
