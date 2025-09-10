package es.ubu.reservapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.ConvocadoPK;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.FranjaHoraria;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.ReservaRepo;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepo reservaRepo;
    private final UsuarioRepo usuarioRepo;
    private final ConvocatoriaService convocatoriaService;
    private final EmailService emailService;

    /**
     * Constructor para la inyección de dependencias.
     * 
     * @param reservaRepo
     * @param usuarioRepo
     * @param convocatoriaService
     * @param emailService
     */
    public ReservaServiceImpl(ReservaRepo reservaRepo, UsuarioRepo usuarioRepo, ConvocatoriaService convocatoriaService, EmailService emailService) {
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
        this.convocatoriaService = convocatoriaService;
        this.emailService = emailService;
    }

    @Override
    public Reserva crearReservaConConvocatorias(Reserva reserva, Usuario usuarioQueReserva, List<String> idUsuariosConvocados) throws UserNotFoundException {
        reserva.setUsuario(usuarioQueReserva);
        
        // Guardar la reserva principal primero para obtener su ID generado
        Reserva reservaGuardada = reservaRepo.save(reserva);

        // Solo crear convocatoria si hay usuarios convocados
        if (idUsuariosConvocados != null && !idUsuariosConvocados.isEmpty()) {
            Convocatoria convocatoria = new Convocatoria();
            convocatoria.setReserva(reservaGuardada);
            List<Convocado> convocatorias = new ArrayList<>();
            
            for (String idUsuarioConvocado : idUsuariosConvocados) {
                Optional<Usuario> optUsuarioConvocado = usuarioRepo.findById(idUsuarioConvocado);
                if (optUsuarioConvocado.isPresent()) {
                    Usuario usuarioConvocado = optUsuarioConvocado.get();

                    ConvocadoPK convocadoPK = new ConvocadoPK(reservaGuardada.getId(), usuarioConvocado.getId());
                    Convocado convocado = new Convocado();
                    convocado.setId(convocadoPK);
                    convocado.setUsuario(usuarioConvocado);
                    
                    convocatorias.add(convocado);
                } else {
                	log.error("Usuario convocado con ID " + idUsuarioConvocado + " no encontrado.");
                	throw new UserNotFoundException("El Usuario con ID " + idUsuarioConvocado + " no fue encontrado.");
                }
            }
            convocatoria.setConvocados(convocatorias);
            reservaGuardada.setConvocatoria(convocatoria);
		}

		// Enviar correo de confirmación al usuario que reserva
		emailService.enviarNotificacionReservaCreada(reservaGuardada);

		return reservaGuardada;
	}

    @Override
    public List<Reserva> findAll() {
        return reservaRepo.findAll();
    }

    @Override
    public Reserva save(Reserva reserva) {
        return reservaRepo.save(reserva);
    }
    
    @Override
    public List<Reserva> findByUsuario(Usuario usuario) {
        return reservaRepo.findByUsuario(usuario);
    }

	@Override
	public List<Reserva> findByEstablecimientoAndFechaReservaBetween(Establecimiento establecimiento, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		List<Reserva> reservas = reservaRepo.findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin);
		return obtenerConvocatorias(reservas);
	}

	@Override
	public List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaBefore(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual) {
		List<Reserva> reservas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual); 
		return obtenerConvocatorias(reservas);
	}

	@Override
	public List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual) {
		List<Reserva> reservas = reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual); 
		return obtenerConvocatorias(reservas);
	}

	@Override
	public Reserva findById(Integer id) {
		Reserva reserva = reservaRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Reserva con ID " + id + " no encontrada."));
		reserva.getEstablecimiento().getId();
        if (reserva.getEstablecimiento().getFranjasHorarias() != null) {
        	reserva.getEstablecimiento().setFranjasHorarias(new ArrayList<>(reserva.getEstablecimiento().getFranjasHorarias()));
        	reserva.getEstablecimiento().getFranjasHorarias().sort(Comparator.comparing(FranjaHoraria::getDiaSemana));
        }

		return obtenerConvocatoria(reserva);
	}

	@Override
	public boolean verificarDisponibilidad(Establecimiento establecimiento, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Reserva reservaExcluir) {
	    // Si el aforo es 0 o negativo, no hay límite de capacidad
	    if (establecimiento.getAforo() == null || establecimiento.getAforo() <= 0) {
	        return true;
	    }

	    LocalDateTime fechaInicio = LocalDateTime.of(fecha, horaInicio);
	    LocalDateTime fechaFin = LocalDateTime.of(fecha, horaFin);
	    
	    // Obtener reservas que se solapan con el horario solicitado
	    List<Reserva> reservasSolapadas = reservaRepo.findReservasSolapadas(
	        establecimiento, 
	        fechaInicio, 
	        fechaFin
	    );
	    
	    // Excluir la reserva especificada si existe (para casos de edición)
	    if (reservaExcluir != null) {
	        reservasSolapadas = reservasSolapadas.stream()
	            .filter(r -> !r.getId().equals(reservaExcluir.getId()))
	            .toList();
	    }

	    // Verificar si el número de reservas solapadas es menor que el aforo
	    int reservasExistentes = reservasSolapadas.size();
	    boolean disponible = reservasExistentes < establecimiento.getAforo();
	    
	    log.debug("Verificando disponibilidad para establecimiento {} en fecha {} de {} a {}: " +
	             "Reservas existentes: {}, Aforo: {}, Disponible: {}", 
	             establecimiento.getNombre(), fecha, horaInicio, horaFin, 
	             reservasExistentes, establecimiento.getAforo(), disponible);
	    
	    return disponible;
	}

	@Override
	public List<FranjaDisponibilidad> obtenerFranjasDisponibles(Establecimiento establecimiento, LocalDate fecha) {
	    List<FranjaDisponibilidad> franjasDisponibles = new ArrayList<>();
	    
	    // Obtener todas las reservas del día
	    LocalDateTime inicioDelDia = fecha.atStartOfDay();
	    LocalDateTime finDelDia = fecha.atTime(23, 59, 59);
	    List<Reserva> reservasDelDia = reservaRepo.findReservasByEstablecimientoAndFecha(
	        establecimiento, inicioDelDia, finDelDia);
	    
	    // Procesar cada franja horaria del establecimiento
	    for (FranjaHoraria franja : establecimiento.getFranjasHorarias()) {
	        if (franja.getDiaSemana().equals(fecha.getDayOfWeek())) {
	            List<PeriodoDisponible> periodosDisponibles = calcularPeriodosDisponibles(franja, reservasDelDia, establecimiento.getAforo());
	            franjasDisponibles.add(new FranjaDisponibilidad(franja, periodosDisponibles));
	        }
	    }
	    
	    return franjasDisponibles;
	}

	@Override
	public List<Reserva> obtenerReservasSolapadas(Establecimiento establecimiento, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
	    LocalDateTime fechaInicio = LocalDateTime.of(fecha, horaInicio);
	    LocalDateTime fechaFin = LocalDateTime.of(fecha, horaFin);
	    
	    return reservaRepo.findReservasSolapadas(establecimiento, fechaInicio, fechaFin );
	}

	@Override
	public DisponibilidadDia obtenerDisponibilidadDia(Establecimiento establecimiento, LocalDate fecha) {
	    // Verificar si el establecimiento tiene horario de apertura para este día
	    boolean tieneHorarioApertura = establecimiento.getFranjasHorarias().stream()
	        .anyMatch(franja -> franja.getDiaSemana().equals(fecha.getDayOfWeek()));
	    
	    if (!tieneHorarioApertura) {
	        return new DisponibilidadDia(fecha, false, false, List.of(), "Cerrado");
	    }
	    
	    // Obtener franjas disponibles para el día
	    List<FranjaDisponibilidad> franjasDisponibles = obtenerFranjasDisponibles(establecimiento, fecha);
	    
	    // Verificar si hay disponibilidad
	    boolean tieneDisponibilidad = franjasDisponibles.stream()
	        .anyMatch(FranjaDisponibilidad::isTieneDisponibilidad);
	    
	    // Generar resumen
	    String resumen;
	    if (!tieneDisponibilidad) {
	        resumen = "Sin disponibilidad";
	    } else {
	        long totalPeriodos = franjasDisponibles.stream()
	            .mapToLong(franja -> franja.getPeriodosDisponibles().size())
	            .sum();
	        resumen = totalPeriodos == 1 ? "1 período disponible" : totalPeriodos + " períodos disponibles";
	    }
	    
	    return new DisponibilidadDia(fecha, tieneHorarioApertura, tieneDisponibilidad, franjasDisponibles, resumen);
	}

	@Override
	public Map<LocalDate, DisponibilidadDia> obtenerDisponibilidadMensual(Establecimiento establecimiento, int anyo, int mes) {
	    Map<LocalDate, DisponibilidadDia> disponibilidadMensual = new HashMap<>();
	    
	    // Obtener el primer y último día del mes
	    LocalDate primerDia = LocalDate.of(anyo, mes, 1);
	    LocalDate ultimoDia = primerDia.withDayOfMonth(primerDia.lengthOfMonth());
	    
	    // Obtener todas las reservas del mes de una sola vez para optimizar
	    LocalDateTime inicioMes = primerDia.atStartOfDay();
	    LocalDateTime finMes = ultimoDia.atTime(23, 59, 59);
	    List<Reserva> reservasDelMes = reservaRepo.findReservasByEstablecimientoAndFecha(
	        establecimiento, inicioMes, finMes);
	    
	    // Agrupar reservas por fecha para optimizar consultas
	    Map<LocalDate, List<Reserva>> reservasPorFecha = reservasDelMes.stream()
	        .collect(Collectors.groupingBy(reserva -> reserva.getFechaReserva().toLocalDate()));
	    
	    // Calcular disponibilidad para cada día del mes
	    LocalDate fechaActual = primerDia;
	    while (!fechaActual.isAfter(ultimoDia)) {
	        DisponibilidadDia disponibilidad = calcularDisponibilidadDiaOptimizada(
	            establecimiento, fechaActual, reservasPorFecha.getOrDefault(fechaActual, List.of()));
	        disponibilidadMensual.put(fechaActual, disponibilidad);
	        fechaActual = fechaActual.plusDays(1);
	    }
	    
	    return disponibilidadMensual;
	}

	/**
	 * Calcula los períodos disponibles dentro de una franja horaria considerando
	 * las reservas existentes y el aforo. Si el aforo es ilimitado, devuelve toda
	 * la franja como disponible.
	 * 
	 * @param franja         franja horaria a evaluar
	 * @param reservasDelDia reservas del día que se están considerando
	 * @param aforo          aforo del establecimiento
	 * @return lista de períodos disponibles dentro de la franja
	 */
	private List<PeriodoDisponible> calcularPeriodosDisponibles(FranjaHoraria franja, List<Reserva> reservasDelDia, Integer aforo) {
	    if (esAforoIlimitado(aforo)) {
	        return List.of(new PeriodoDisponible(franja.getHoraInicio(), franja.getHoraFin()));
	    }
	    
	    List<Reserva> reservasEnFranja = filtrarReservasEnFranja(reservasDelDia, franja);
	    
	    if (reservasEnFranja.isEmpty()) {
	        return List.of(new PeriodoDisponible(franja.getHoraInicio(), franja.getHoraFin()));
	    }
	    
	    List<LocalTime> puntosDeTime = obtenerPuntosDeTiempoOrdenados(franja, reservasEnFranja);
	    
	    return calcularPeriodosEntreIntervalos(puntosDeTime, reservasEnFranja, aforo);
	}

	/**
	 * Calcula la disponibilidad de un día específico usando las reservas ya cargadas.
	 * Versión optimizada que evita consultas adicionales a la base de datos.
	 * 
	 * @param establecimiento Establecimiento a verificar
	 * @param fecha Fecha a verificar
	 * @param reservasDelDia Reservas ya cargadas para ese día
	 * @return Información de disponibilidad del día
	 */
	private DisponibilidadDia calcularDisponibilidadDiaOptimizada(Establecimiento establecimiento, 
	                                                             LocalDate fecha, 
	                                                             List<Reserva> reservasDelDia) {
	    // Verificar si el establecimiento tiene horario de apertura para este día
	    boolean tieneHorarioApertura = establecimiento.getFranjasHorarias().stream()
	        .anyMatch(franja -> franja.getDiaSemana().equals(fecha.getDayOfWeek()));
	    
	    if (!tieneHorarioApertura) {
	        return new DisponibilidadDia(fecha, false, false, List.of(), "Cerrado");
	    }
	    
	    // Calcular franjas disponibles usando las reservas ya cargadas
	    List<FranjaDisponibilidad> franjasDisponibles = new ArrayList<>();
	    Integer aforo = establecimiento.getAforo();
	    
	    for (FranjaHoraria franja : establecimiento.getFranjasHorarias()) {
	        if (franja.getDiaSemana().equals(fecha.getDayOfWeek())) {
	            List<PeriodoDisponible> periodosDisponibles = calcularPeriodosDisponibles(franja, reservasDelDia, aforo);
	            franjasDisponibles.add(new FranjaDisponibilidad(franja, periodosDisponibles));
	        }
	    }
	    
	    // Verificar si hay disponibilidad
	    boolean tieneDisponibilidad = franjasDisponibles.stream()
	        .anyMatch(FranjaDisponibilidad::isTieneDisponibilidad);
	    
	    // Generar resumen
	    String resumen;
	    if (!tieneDisponibilidad) {
	        resumen = "Sin disponibilidad";
	    } else {
	        long totalPeriodos = franjasDisponibles.stream()
	            .mapToLong(franja -> franja.getPeriodosDisponibles().size())
	            .sum();
	        resumen = totalPeriodos == 1 ? "1 período disponible" : totalPeriodos + " períodos disponibles";
	    }
	    
	    return new DisponibilidadDia(fecha, tieneHorarioApertura, tieneDisponibilidad, franjasDisponibles, resumen);
	}

	/**
	 * Verifica si el aforo es ilimitado (nulo o menor/igual a 0).
	 * 
	 * @param aforo el aforo a verificar
	 * @return true si el aforo es ilimitado, false en caso contrario
	 */
	private boolean esAforoIlimitado(Integer aforo) {
	    return aforo == null || aforo <= 0;
	}

	/**
	 * Filtra las reservas que se solapan con la franja horaria especificada.
	 * 
	 * @param reservasDelDia reservas del día que se están considerando
	 * @param franja         franja horaria a filtrar
	 * @return lista de reservas que se solapan con la franja
	 */
	private List<Reserva> filtrarReservasEnFranja(List<Reserva> reservasDelDia, FranjaHoraria franja) {
	    return reservasDelDia.stream()
	        .filter(reserva -> sesolapaConFranja(reserva, franja))
	        .toList();
	}

	/**
	 * Verifica si una reserva se solapa con una franja horaria.
	 * 
	 * @param reserva la reserva a verificar
	 * @param franja  la franja horaria a comparar
	 * @return true si la reserva se solapa con la franja, false en caso contrario
	 */
	private boolean sesolapaConFranja(Reserva reserva, FranjaHoraria franja) {
	    LocalTime horaInicioReserva = reserva.getFechaReserva().toLocalTime();
	    LocalTime horaFinReserva = obtenerHoraFinReserva(reserva);
	    
	    return !(horaFinReserva.isBefore(franja.getHoraInicio()) || 
	            horaInicioReserva.isAfter(franja.getHoraFin()));
	}

	/**
	 * Obtiene la hora de fin de una reserva, usando hora por defecto si no está
	 * especificada.
	 * 
	 * @param reserva la reserva de la cual obtener la hora de fin
	 * @return la hora de fin de la reserva, o una hora por defecto (1 hora después
	 *         de la hora de inicio) si no está especificada
	 */
	private LocalTime obtenerHoraFinReserva(Reserva reserva) {
	    return reserva.getHoraFin() != null ? 
	           reserva.getHoraFin() : 
	           reserva.getFechaReserva().toLocalTime().plusHours(1);
	}

	/**
	 * Obtiene todos los puntos de tiempo relevantes (inicio/fin de franja y
	 * reservas) ordenados.
	 * 
	 * @param franja           la franja horaria a evaluar
	 * @param reservasEnFranja las reservas que se solapan con la franja
	 * @return lista de puntos de tiempo ordenados
	 */
	private List<LocalTime> obtenerPuntosDeTiempoOrdenados(FranjaHoraria franja, List<Reserva> reservasEnFranja) {
	    List<LocalTime> puntosDeTime = new ArrayList<>();
	    puntosDeTime.add(franja.getHoraInicio());
	    puntosDeTime.add(franja.getHoraFin());
	    
	    reservasEnFranja.forEach(reserva -> {
	        LocalTime horaInicioReserva = reserva.getFechaReserva().toLocalTime();
	        LocalTime horaFinReserva = obtenerHoraFinReserva(reserva);
	        
	        agregarPuntoSiEstaEnFranja(puntosDeTime, horaInicioReserva, franja);
	        agregarPuntoSiEstaEnFranja(puntosDeTime, horaFinReserva, franja);
	    });
	    
	    return puntosDeTime.stream()
	        .distinct()
	        .sorted()
	        .toList();
	}

	/**
	 * Agrega un punto de tiempo a la lista si está dentro de la franja horaria.
	 * 
	 * @param puntosDeTime la lista de puntos de tiempo
	 * @param punto        el punto de tiempo a agregar
	 * @param franja       la franja horaria a verificar
	 */
	private void agregarPuntoSiEstaEnFranja(List<LocalTime> puntosDeTime, LocalTime punto, FranjaHoraria franja) {
	    if (estaEntreFranja(punto, franja)) {
	        puntosDeTime.add(punto);
	    }
	}

	/**
	 * Verifica si un punto de tiempo está dentro de la franja horaria.
	 * 
	 * @param punto  el punto de tiempo a verificar
	 * @param franja la franja horaria a comparar
	 * @return true si el punto está dentro de la franja, false en caso contrario
	 */
	private boolean estaEntreFranja(LocalTime punto, FranjaHoraria franja) {
	    return !punto.isBefore(franja.getHoraInicio()) && !punto.isAfter(franja.getHoraFin());
	}

	/**
	 * Calcula los períodos disponibles entre puntos de tiempo consecutivos. Si el
	 * número de reservas en un intervalo es menor que el aforo, se considera
	 * disponible.
	 * 
	 * @param puntosDeTime     lista de puntos de tiempo ordenados
	 * @param reservasEnFranja reservas que se solapan con la franja
	 * @param aforo            aforo del establecimiento
	 * @return lista de períodos disponibles
	 */
	private List<PeriodoDisponible> calcularPeriodosEntreIntervalos(List<LocalTime> puntosDeTime, List<Reserva> reservasEnFranja, Integer aforo) {
	    List<PeriodoDisponible> periodosDisponibles = new ArrayList<>();
	    
	    for (int i = 0; i < puntosDeTime.size() - 1; i++) {
	        LocalTime inicioIntervalo = puntosDeTime.get(i);
	        LocalTime finIntervalo = puntosDeTime.get(i + 1);
	        
	        long reservasEnIntervalo = contarReservasEnIntervalo(reservasEnFranja, inicioIntervalo, finIntervalo);
	        
	        if (reservasEnIntervalo < aforo) {
	            periodosDisponibles.add(new PeriodoDisponible(inicioIntervalo, finIntervalo));
	        }
	    }
	    
	    return periodosDisponibles;
	}

	/**
	 * Cuenta cuántas reservas ocupan un intervalo de tiempo específico.
	 * 
	 * @param reservasEnFranja lista de reservas que se solapan con la franja
	 * @param inicioIntervalo  hora de inicio del intervalo
	 * @param finIntervalo     hora de fin del intervalo
	 * @return el número de reservas que ocupan el intervalo
	 */
	private long contarReservasEnIntervalo(List<Reserva> reservasEnFranja, LocalTime inicioIntervalo, LocalTime finIntervalo) {
	    return reservasEnFranja.stream()
	        .filter(reserva -> reservaOcupaIntervalo(reserva, inicioIntervalo, finIntervalo))
	        .count();
	}

	/**
	 * Verifica si una reserva ocupa un intervalo de tiempo específico.
	 * 
	 * @param reserva         la reserva a verificar
	 * @param inicioIntervalo hora de inicio del intervalo
	 * @param finIntervalo    hora de fin del intervalo
	 * @return true si la reserva ocupa el intervalo, false en caso contrario
	 */
	private boolean reservaOcupaIntervalo(Reserva reserva, LocalTime inicioIntervalo, LocalTime finIntervalo) {
	    LocalTime horaInicioReserva = reserva.getFechaReserva().toLocalTime();
	    LocalTime horaFinReserva = obtenerHoraFinReserva(reserva);
	    
	    return !(horaFinReserva.isBefore(inicioIntervalo) || horaInicioReserva.isAfter(finIntervalo));
	}

	/**
	 * Recupera las convocatorias de las reserva. Este método se asegura de que cada
	 * reserva tenga su convocatoria cargada, y que cada convocado tenga su usuario
	 * cargado.
	 * 
	 * @param reservas
	 * @return lista de reservas con sus convocatorias cargadas
	 */
	private List<Reserva> obtenerConvocatorias(List<Reserva> reservas) {
		for (Reserva reserva : reservas) {
			obtenerConvocatoria(reserva);
        }
		return reservas;
	}

	/**
	 * Recupera la convocatoria de una reserva. Si la reserva no tiene una
	 * convocatoria cargada, intenta cargarla desde la base de datos. Además, se
	 * asegura de que cada convocado tenga su usuario cargado.
	 * 
	 * @param reserva reserva a la que se le quiere cargar la convocatoria
	 * @return reserva con su convocatoria cargada
	 */
	private Reserva obtenerConvocatoria(Reserva reserva) {
		if (reserva.getConvocatoria() == null) {
			Convocatoria convocatoria = convocatoriaService.findByIdIgnoringValido(reserva.getId());
			if (convocatoria != null) {
				reserva.setConvocatoria(convocatoria);
			}
		}
		
		if (reserva.getConvocatoria() != null && reserva.getConvocatoria().getConvocados() != null) {
			reserva.getConvocatoria().setConvocados(new ArrayList<>(reserva.getConvocatoria().getConvocados()));

			for (Convocado convocado : reserva.getConvocatoria().getConvocados()) {
				convocado.setUsuario(convocado.getUsuario());
			}
		}
		return reserva;
	}

	@Override
	public void delete(Reserva reserva) {
		reservaRepo.delete(reserva);
	}
}