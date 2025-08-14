package es.ubu.reservapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.Convocado;
import es.ubu.reservapp.model.entities.ConvocadoPK;
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
	            List<PeriodoDisponible> periodosDisponibles = calcularPeriodosDisponibles(
	                franja, reservasDelDia, establecimiento.getAforo());
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

	/**
	 * Calcula los períodos disponibles dentro de una franja horaria considerando las reservas existentes y el aforo.
	 */
	private List<PeriodoDisponible> calcularPeriodosDisponibles(FranjaHoraria franja, List<Reserva> reservasDelDia, Integer aforo) {
	    List<PeriodoDisponible> periodosDisponibles = new ArrayList<>();
	    
	    // Si no hay límite de aforo, toda la franja está disponible
	    if (aforo == null || aforo <= 0) {
	        periodosDisponibles.add(new PeriodoDisponible(franja.getHoraInicio(), franja.getHoraFin()));
	        return periodosDisponibles;
	    }
	    
	    // Filtrar reservas que afectan a esta franja horaria
	    List<Reserva> reservasEnFranja = reservasDelDia.stream()
	        .filter(r -> {
	            LocalTime horaInicioReserva = r.getFechaReserva().toLocalTime();
	            LocalTime horaFinReserva = r.getHoraFin() != null ? r.getHoraFin() : horaInicioReserva.plusHours(1);
	            
	            // Verificar si la reserva se solapa con la franja
	            return !(horaFinReserva.isBefore(franja.getHoraInicio()) || 
	                    horaInicioReserva.isAfter(franja.getHoraFin()));
	        }).toList();
	    
	    // Si no hay reservas en la franja, toda está disponible
	    if (reservasEnFranja.isEmpty()) {
	        periodosDisponibles.add(new PeriodoDisponible(franja.getHoraInicio(), franja.getHoraFin()));
	        return periodosDisponibles;
	    }
	    
	    // Crear una lista de todos los puntos de tiempo relevantes
	    List<LocalTime> puntosDeTime = new ArrayList<>();
	    puntosDeTime.add(franja.getHoraInicio());
	    puntosDeTime.add(franja.getHoraFin());
	    
	    for (Reserva reserva : reservasEnFranja) {
	        LocalTime horaInicioReserva = reserva.getFechaReserva().toLocalTime();
	        LocalTime horaFinReserva = reserva.getHoraFin() != null ? reserva.getHoraFin() : horaInicioReserva.plusHours(1);
	        
	        if (!horaInicioReserva.isBefore(franja.getHoraInicio()) && !horaInicioReserva.isAfter(franja.getHoraFin())) {
	            puntosDeTime.add(horaInicioReserva);
	        }
	        if (!horaFinReserva.isBefore(franja.getHoraInicio()) && !horaFinReserva.isAfter(franja.getHoraFin())) {
	            puntosDeTime.add(horaFinReserva);
	        }
	    }
	    
	    // Ordenar y eliminar duplicados
	    puntosDeTime = puntosDeTime.stream()
	        .distinct()
	        .sorted()
	        .collect(Collectors.toList());
	    
	    // Verificar cada intervalo entre puntos consecutivos
	    for (int i = 0; i < puntosDeTime.size() - 1; i++) {
	        LocalTime inicioIntervalo = puntosDeTime.get(i);
	        LocalTime finIntervalo = puntosDeTime.get(i + 1);
	        
	        // Contar cuántas reservas ocupan este intervalo
	        long reservasEnIntervalo = reservasEnFranja.stream()
	            .filter(r -> {
	                LocalTime horaInicioReserva = r.getFechaReserva().toLocalTime();
	                LocalTime horaFinReserva = r.getHoraFin() != null ? r.getHoraFin() : horaInicioReserva.plusHours(1);
	                
	                // La reserva ocupa el intervalo si se solapa con él
	                return !(horaFinReserva.isBefore(inicioIntervalo) || horaInicioReserva.isAfter(finIntervalo));
	            })
	            .count();
	        
	        // Si hay capacidad disponible, agregar el período
	        if (reservasEnIntervalo < aforo) {
	            periodosDisponibles.add(new PeriodoDisponible(inicioIntervalo, finIntervalo));
	        }
	    }
	    
	    return periodosDisponibles;
	}

	/**
	 * Recupera las convocatorias de las reserva
	 * 
	 * @param reservas
	 */
	private List<Reserva> obtenerConvocatorias(List<Reserva> reservas) {
		for (Reserva reserva : reservas) {
			obtenerConvocatoria(reserva);
        }
		return reservas;
	}

	private Reserva obtenerConvocatoria(Reserva reserva) {
		// Si no hay convocatoria cargada, intentar cargarla desde la base de datos
		if (reserva.getConvocatoria() == null) {
			Convocatoria convocatoria = convocatoriaService.findByIdIgnoringValido(reserva.getId());
			if (convocatoria != null) {
				reserva.setConvocatoria(convocatoria);
			}
		}
		
		// Procesar la convocatoria si existe
		if (reserva.getConvocatoria() != null && reserva.getConvocatoria().getConvocados() != null) {
			// Asegurarse de que la lista de convocados no sea nula y convertirla a una lista mutable
			reserva.getConvocatoria().setConvocados(new ArrayList<>(reserva.getConvocatoria().getConvocados()));

			// Asegurarse de que cada convocado tenga su usuario cargado
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