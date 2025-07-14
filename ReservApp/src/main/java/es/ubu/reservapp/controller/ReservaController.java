package es.ubu.reservapp.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
    private static final String REDIRECT_RESERVAS_ESTABLECIMIENTO = REDIRECT + "misreservas/establecimiento/";
    private static final String ERROR = "error";
    private static final String EXITO = "exito";
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

		Convocatoria convocatoria = new Convocatoria();
		convocatoria.setReserva(reserva);
		convocatoria.setEnlace(enlaceReunion);
		convocatoria.setObservaciones(observaciones);
		reserva.setConvocatoria(convocatoria);
		return guardarReservaConConvocatoria(reserva, establecimiento, horario, usuariosConvocados, redirectAttributes);
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
            
            // Enviar correo de confirmación al usuario que crea la reserva
            try {
                emailService.enviarNotificacionReservaCreada(reservaGuardada);
                log.info("Correo de confirmación de reserva enviado al usuario: {}", reservaGuardada.getUsuario().getCorreo());
            } catch (Exception e) {
                log.error("Error al enviar correo de confirmación de reserva: {}", e.getMessage(), e);
                // No interrumpir el flujo, solo registrar el error
            }
            
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
     * Gestiona las convocatorias considerando el soft delete.
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
        
        for (String usuarioId : usuariosConvocados) {
            if (usuarioId != null && !usuarioId.trim().isEmpty()) {
                Usuario usuario = usuarioService.findUsuarioById(usuarioId.trim());
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
        
        for (String usuarioId : usuariosConvocados) {
            if (usuarioId != null && !usuarioId.trim().isEmpty()) {
                Usuario usuario = usuarioService.findUsuarioById(usuarioId.trim());
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
     * Endpoint para buscar usuarios por nombre o ID (AJAX).
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
        
        List<Usuario> todosUsuarios = usuarioService.findAll();
        String queryLower = query.toLowerCase().trim();
        
        return todosUsuarios.stream()
                .filter(usuario -> !usuario.getId().equals(sessionData.getUsuario().getId())) // Excluir usuario actual
                .filter(usuario -> 
                    usuario.getId().toLowerCase().contains(queryLower) ||
                    usuario.getNombre().toLowerCase().contains(queryLower) ||
                    usuario.getApellidos().toLowerCase().contains(queryLower) ||
                    usuario.getCorreo().toLowerCase().contains(queryLower)
                )
                .limit(10) // Limitar resultados
                .map(usuario -> new UsuarioDTO(usuario.getId(), 
                    usuario.getNombre() + " " + usuario.getApellidos(), 
                    usuario.getCorreo())).toList();
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
                redirectAttributes.addFlashAttribute(ERROR, "Reserva no encontrada.");
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
                redirectAttributes.addFlashAttribute(ERROR, "Reserva no encontrada.");
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
        
        List<Reserva> reservasPasadas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
        List<Reserva> reservasFuturas = reservaService.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
        
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
}