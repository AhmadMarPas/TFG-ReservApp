package es.ubu.reservapp.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
                               @RequestParam(value = "enlaceReunion", required = false) String enlaceReunion,
                               @RequestParam(value = "observaciones", required = false) String observaciones,
                               @RequestParam(value = "usuariosConvocados", required = false) String[] usuariosConvocados,
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

		// NUEVA VALIDACIÓN: Verificar disponibilidad considerando aforo
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
            // Validar usuario autenticado
            if (sessionData.getUsuario() == null) {
                return ResponseEntity.badRequest().build();
            }

            // Obtener establecimiento
            Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
            if (establecimientoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Establecimiento establecimiento = establecimientoOpt.get();
            LocalDate fechaReserva = LocalDate.parse(fecha);
            DayOfWeek diaSemana = fechaReserva.getDayOfWeek();

            // Obtener reservas del día
            LocalDateTime inicioDelDia = fechaReserva.atStartOfDay();
            LocalDateTime finDelDia = fechaReserva.atTime(23, 59, 59);
            List<Reserva> reservasDelDia = reservaService.findByEstablecimientoAndFechaReservaBetween(establecimiento, inicioDelDia, finDelDia);
            
            // Excluir la reserva actual si se está editando
            if (reservaId != null) {
                reservasDelDia = reservasDelDia.stream()
                    .filter(reserva -> !reserva.getId().equals(reservaId)).toList();
            }

            // Generar slots para el día específico
            List<SlotReservaUtil.SlotTiempo> slotsDisponibles = new ArrayList<>();
            
            for (FranjaHoraria franja : establecimiento.getFranjasHorarias()) {
                if (franja.getDiaSemana().equals(diaSemana)) {
                    List<SlotReservaUtil.SlotTiempo> slotsFranja = SlotReservaUtil.generarSlotsConDisponibilidad(establecimiento, franja, reservasDelDia);
                    slotsDisponibles.addAll(slotsFranja);
                }
            }

            // Ordenar slots por hora
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
    public ResponseEntity<FranjasDisponiblesResponse> obtenerFranjasDisponibles(
            @RequestParam Integer establecimientoId,
            @RequestParam String fecha) {
        
        try {
            // Validar usuario autenticado
            if (sessionData.getUsuario() == null) {
                return ResponseEntity.badRequest().build();
            }

            // Obtener establecimiento
            Optional<Establecimiento> establecimientoOpt = establecimientoService.findById(establecimientoId);
            if (establecimientoOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Establecimiento establecimiento = establecimientoOpt.get();
            LocalDate fechaReserva = LocalDate.parse(fecha);

            // Obtener franjas disponibles
            List<ReservaService.FranjaDisponibilidad> franjasDisponibles = 
                reservaService.obtenerFranjasDisponibles(establecimiento, fechaReserva);

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
        
        // OPTIMIZACIÓN: Usar búsqueda optimizada en base de datos en lugar de cargar todos los usuarios
        List<Usuario> usuarios = usuarioService.buscarUsuarioSegunQuery(queryLower);
        
        return usuarios.stream()
                .map(usuario -> new UsuarioDTO(usuario.getId(), 
                    usuario.getNombre() + " " + usuario.getApellidos(), 
                    usuario.getCorreo()))
                .toList();
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
            // Guardar la reserva primero
            Reserva reservaGuardada = reservaService.save(reserva);
            
            // Crear convocatorias si hay usuarios seleccionados
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
		// Enviar correo de confirmación al usuario que crea la reserva
		try {
		    emailService.enviarNotificacionReservaCreada(reservaGuardada);
		    log.info("Correo de confirmación de reserva enviado al usuario: {}", reservaGuardada.getUsuario().getCorreo());
		} catch (Exception e) {
		    log.error("Error al enviar correo de confirmación de reserva: {}", e.getMessage(), e);
		    // No interrumpir el flujo, solo registrar el error
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
        // Eliminar todas las convocatorias existentes
        convocatoriaService.deleteByReserva(reserva);
        
        // Si no hay usuarios convocados, no hacer nada más
        if (usuariosConvocados == null || usuariosConvocados.length == 0) {
            return;
        }
        
        List<Convocado> convocatoriasActivas = new ArrayList<>();
        
        // Crear siempre una nueva convocatoria para evitar conflictos
        Convocatoria nuevaConvocatoria = new Convocatoria();
        nuevaConvocatoria.setReserva(reserva);
        reserva.setConvocatoria(nuevaConvocatoria);
        nuevaConvocatoria.setConvocados(new ArrayList<>());
        log.info("Creando nueva convocatoria para reserva: {}", reserva.getId());
        
        // OPTIMIZACIÓN: Filtrar IDs válidos y cargar usuarios en una sola consulta
        List<String> idsValidos = new ArrayList<>();
        for (String usuarioId : usuariosConvocados) {
            if (usuarioId != null && !usuarioId.trim().isEmpty()) {
                idsValidos.add(usuarioId.trim());
            }
        }
        
        if (!idsValidos.isEmpty()) {
            // Cargar todos los usuarios de una vez en lugar de consultas individuales
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
                    
                    // Añadir el convocado a la lista de la convocatoria
                    nuevaConvocatoria.getConvocados().add(convocado);
                    convocatoriasActivas.add(convocado);
                    log.info("Convocado creado para usuario: {} en reserva: {}", usuario.getId(), reserva.getId());
                }
            }
        }
        
        // Guardar la convocatoria solo si hay convocados
        if (!convocatoriasActivas.isEmpty()) {
            convocatoriaService.save(nuevaConvocatoria);
            
            // Enviar notificaciones por correo electrónico
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
        
        // Crear convocatoria solo si no existe y hay usuarios convocados
        Convocatoria convocatoria = reserva.getConvocatoria();
        if (convocatoria == null) {
            convocatoria = new Convocatoria();
            convocatoria.setReserva(reserva);
            reserva.setConvocatoria(convocatoria);
        }
        
        // Inicializar lista de convocados si no existe
        if (convocatoria.getConvocados() == null) {
            convocatoria.setConvocados(new ArrayList<>());
        }
        
        // OPTIMIZACIÓN: Filtrar IDs válidos y cargar usuarios en una sola consulta
        List<String> idsValidos = new ArrayList<>();
        for (String usuarioId : usuariosConvocados) {
            if (usuarioId != null && !usuarioId.trim().isEmpty()) {
                idsValidos.add(usuarioId.trim());
            }
        }
        
        if (!idsValidos.isEmpty()) {
            // Cargar todos los usuarios de una vez en lugar de consultas individuales
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

                    // Añadir el convocado a la lista de la convocatoria
                    convocatoria.getConvocados().add(convocado);
                    convocatoriasCreadas.add(convocado);
                    log.info("Convocatoria creada para usuario: {} en reserva: {}", usuario.getId(), reserva.getId());
                }
            }
        }
        
        // Guardar la convocatoria solo una vez al final (el cascade guardará los convocados)
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
		// Enviar notificaciones por correo electrónico
        if (!convocatoriasCreadas.isEmpty()) {
            try {
                emailService.enviarNotificacionesConvocatoria(convocatoriasCreadas, reserva);
                log.info("Notificaciones de convocatoria enviadas para {} usuarios", convocatoriasCreadas.size());
            } catch (Exception e) {
                log.error("Error al enviar notificaciones de convocatoria: {}", e.getMessage(), e);
                // No interrumpir el flujo, solo registrar el error
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
            // Validar usuario autenticado
            String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
            if (errorUsuario != null) return errorUsuario;
            
            Usuario usuario = sessionData.getUsuario();
            
            Reserva reserva = reservaService.findById(reservaId);
            if (reserva == null) {
                redirectAttributes.addFlashAttribute(ERROR, RESERVA_NO_ENCONTRADA);
                return REDIRECT_MIS_RESERVAS;
            }
            
            // Log para depuración
            log.info("Reserva cargada para edición - ID: {}, tiene convocatoria: {}", 
                    reservaId, reserva.getConvocatoria() != null);
            if (reserva.getConvocatoria() != null) {
                log.info("Convocatoria - enlace: {}, observaciones: {}, convocados: {}", 
                        reserva.getConvocatoria().getEnlace(),
                        reserva.getConvocatoria().getObservaciones(),
                        reserva.getConvocatoria().getConvocados() != null ? reserva.getConvocatoria().getConvocados().size() : 0);
            }
            
            // Verificar que la reserva pertenece al usuario autenticado
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ERROR, "No tienes permisos para editar esta reserva.");
                return REDIRECT_MIS_RESERVAS;
            }
            
            // Verificar que la reserva es futura
            if (reserva.getFechaReserva().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ERROR, "No se pueden editar reservas pasadas.");
                return REDIRECT_RESERVAS_ESTABLECIMIENTO + reserva.getEstablecimiento().getId();
            }
            
            // Preparar datos para el formulario
            // No llamar a crearReservaConConvocatorias para edición, ya que la reserva ya existe
            model.addAttribute("reserva", reserva);
            model.addAttribute("establecimiento", reserva.getEstablecimiento());
            model.addAttribute("isEdit", true);
			
			// Manejar convocatoria si existe (independientemente de si tiene convocados)
			if (reserva.getConvocatoria() != null) {
				log.info("Agregando convocatoria al modelo para edición - enlace: {}, observaciones: {}", 
						reserva.getConvocatoria().getEnlace(), reserva.getConvocatoria().getObservaciones());
				model.addAttribute("convocatoria", reserva.getConvocatoria());
				
				// Agregar convocados si existen
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
            
            // Obtener franjas horarias del establecimiento
            List<FranjaHoraria> franjasActivas = obtenerFranjasActivas(reserva.getEstablecimiento());
            model.addAttribute("franjasHorarias", franjasActivas);
            
            // Generar slots dinámicos si el establecimiento requiere slots predefinidos
            SlotsData slotsData = generarSlotsData(reserva.getEstablecimiento(), franjasActivas);
            model.addAttribute("requiereSlotsPredefinidos", slotsData.isRequiereSlots());
            model.addAttribute("slotsDisponibles", slotsData.getSlotsDisponibles());
            
            // Si requiere slots predefinidos, calcular el slot actual de la reserva
            if (slotsData.isRequiereSlots()) {
                String slotActual = calcularSlotActual(reserva);
                model.addAttribute("slotActual", slotActual);
            }
            
            return "reservas/editar_reserva";
            
        } catch (Exception e) {
            log.error("Error al mostrar formulario de edición de reserva: ", e);
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
            // Validar usuario autenticado
            String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
            if (errorUsuario != null) return errorUsuario;
            
            Usuario usuario = sessionData.getUsuario();
            
            Reserva reservaExistente = reservaService.findById(reservaId);
            if (reservaExistente == null) {
                redirectAttributes.addFlashAttribute(ERROR, RESERVA_NO_ENCONTRADA);
                return REDIRECT_MIS_RESERVAS;
            }
            
            // Verificar permisos
            if (!reservaExistente.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ERROR, "No tienes permisos para editar esta reserva.");
                return REDIRECT_MIS_RESERVAS;
            }
            
            // Verificar que la reserva es futura
            if (reservaExistente.getFechaReserva().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ERROR, "No se pueden editar reservas pasadas.");
                return REDIRECT_RESERVAS_ESTABLECIMIENTO + reservaExistente.getEstablecimiento().getId();
            }
            
            Establecimiento establecimiento = reservaExistente.getEstablecimiento();
            
            // Procesar horarios
            HorarioReserva horario = parsearHorarios(fechaStr, horaInicioStr, horaFinStr, slotSeleccionado, redirectAttributes);
            if (horario == null) {
                return REDIRECT_MIS_RESERVAS_EDITAR + reservaId;
            }
            
            // Validar franja horaria
            String errorFranja = validarFranjaHoraria(establecimiento, horario, redirectAttributes);
            if (errorFranja != null) {
                return REDIRECT_MIS_RESERVAS_EDITAR + reservaId;
            }
            
            // Verificar disponibilidad considerando aforo
            String errorAforo = validarDisponibilidadAforo(establecimiento, horario, reservaExistente, redirectAttributes);
            if (errorAforo != null) {
                return errorAforo;
            }
            
            // Actualizar la reserva
            reservaExistente.setFechaReserva(LocalDateTime.of(horario.getFecha(), horario.getHoraInicio()));
            reservaExistente.setHoraFin(horario.getHoraFin());
            
            // IMPORTANTE: Determinar si hay datos de convocatoria
			boolean tieneConvocatoria = StringUtils.isNotBlank(enlaceReunion) || StringUtils.isNotBlank(observaciones)
					|| (usuariosConvocados != null && usuariosConvocados.length > 0);
            
            // SIEMPRE limpiar la referencia de convocatoria antes de guardar para evitar problemas de cascada
            reservaExistente.setConvocatoria(null);
            
            // Eliminar convocatorias existentes ANTES de guardar la reserva
            convocatoriaService.deleteByReserva(reservaExistente);
            
            // Guardar la reserva actualizada sin referencias a convocatorias
            Reserva reservaActualizada = reservaService.save(reservaExistente);
            
            // Solo crear/actualizar convocatoria si hay datos de convocatoria o usuarios convocados
            if (tieneConvocatoria) {
                // Crear nueva convocatoria
                Convocatoria convocatoria = new Convocatoria();
                convocatoria.setReserva(reservaActualizada);
                convocatoria.setEnlace(enlaceReunion);
                convocatoria.setObservaciones(observaciones);
                reservaActualizada.setConvocatoria(convocatoria);
                
                // Gestionar convocatorias con soft delete
                gestionarConvocatorias(reservaActualizada, usuariosConvocados);
				convocatoriaService.save(convocatoria);
            }
            
            redirectAttributes.addFlashAttribute(EXITO, "Reserva actualizada correctamente.");
            return REDIRECT_RESERVAS_ESTABLECIMIENTO + establecimiento.getId();
            
        } catch (Exception e) {
            log.error("Error al actualizar reserva: ", e);
            redirectAttributes.addFlashAttribute(ERROR, "Error al actualizar la reserva: " + e.getMessage());
            return REDIRECT_MIS_RESERVAS_EDITAR + reservaId;
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
            // Validar usuario autenticado
            String errorUsuario = validarUsuarioAutenticado(redirectAttributes);
            if (errorUsuario != null) return errorUsuario;
            
            Usuario usuario = sessionData.getUsuario();
            
            // Buscar la reserva
            Reserva reserva = reservaService.findById(reservaId);
            if (reserva == null) {
                redirectAttributes.addFlashAttribute(ERROR, RESERVA_NO_ENCONTRADA);
                return REDIRECT_MIS_RESERVAS;
            }
            
            // Verificar que el usuario es el propietario de la reserva
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute(ERROR, "No tiene permisos para anular esta reserva.");
                return REDIRECT_MIS_RESERVAS;
            }
            
            // Verificar que la reserva es futura
            if (reserva.getFechaReserva().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute(ERROR, "No se pueden anular reservas pasadas.");
                return String.format("redirect:/misreservas/establecimiento/%d", reserva.getEstablecimiento().getId());
            }
            
            // Obtener información para el correo antes de eliminar
            Establecimiento establecimiento = reserva.getEstablecimiento();
            List<String> correosNotificacion = new ArrayList<>();
            
            // Agregar correo del usuario que hizo la reserva
            correosNotificacion.add(usuario.getCorreo());
            
            // Agregar correos de usuarios convocados si existen
            if (reserva.getConvocatoria() != null && reserva.getConvocatoria().getConvocados() != null) {
                reserva.getConvocatoria().getConvocados().forEach(convocado -> {
                    if (convocado.getUsuario() != null && convocado.getUsuario().getCorreo() != null) {
                        correosNotificacion.add(convocado.getUsuario().getCorreo());
                    }
                });
            }
            
            notificarAnulacion(reservaId, reserva, correosNotificacion);
            
            // Eliminar la reserva
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
		// Enviar notificaciones por correo
		try {
		    emailService.enviarNotificacionAnulacion(reserva, correosNotificacion);
		} catch (Exception e) {
		    log.warn("Error al enviar notificaciones de anulación para reserva {}: {}", reservaId, e.getMessage());
		    // Continuar con la anulación aunque falle el envío de correos
		}
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
     * OPTIMIZADO: Limita el número de reservas pasadas para evitar cargar demasiados datos
     * 
     * @param establecimiento
     */
    private ReservasUsuario obtenerReservasUsuario(Usuario usuario, Establecimiento establecimiento) {
        LocalDateTime fechaActual = LocalDateTime.now();
        
        // OPTIMIZACIÓN: Limitar reservas pasadas a las últimas 50 para evitar cargar demasiados datos
        List<Reserva> reservasPasadas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
        List<Reserva> reservasFuturas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
        
        // Las consultas optimizadas ya vienen ordenadas desde la base de datos
        // reservasPasadas ya vienen ordenadas por fecha descendente
        // reservasFuturas se ordenan aquí si es necesario
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
             horaInicio.format(DateTimeFormatter.ofPattern("HH:mm")),
             horaFin.format(DateTimeFormatter.ofPattern("HH:mm")));
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
            
            // Obtener reservas futuras paginadas
            List<Reserva> todasReservasFuturas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
            todasReservasFuturas.sort(Comparator.comparing(Reserva::getFechaReserva));
            
            int inicioFuturas = (paginaFuturas - 1) * tamanyoPagina;
            int finFuturas = Math.min(inicioFuturas + tamanyoPagina, todasReservasFuturas.size());
            List<Reserva> reservasFuturas = inicioFuturas < todasReservasFuturas.size() ? todasReservasFuturas.subList(inicioFuturas, finFuturas) : new ArrayList<>();
            boolean hayMasReservasFuturas = finFuturas < todasReservasFuturas.size();
            
            // Obtener reservas pasadas paginadas
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
}