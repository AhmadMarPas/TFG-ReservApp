package es.ubu.reservapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    /**
     * Constructor para la inyección de dependencias.
     * 
     * @param reservaRepo
     * @param usuarioRepo
     * @param convocatoriaService
     */
    public ReservaServiceImpl(ReservaRepo reservaRepo, UsuarioRepo usuarioRepo) {
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public Reserva crearReservaConConvocatorias(Reserva reserva, Usuario usuarioQueReserva, List<String> idUsuariosConvocados) throws UserNotFoundException {
        reserva.setUsuario(usuarioQueReserva);
        
        // Si la reserva ya tiene ID, es una edición, no crear nueva convocatoria automáticamente
        if (reserva.getId() != null) {
            return reserva;
        }
        
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

		return reserva;
	}

	/**
	 * Recupera las convocatorias de las reserva
	 * 
	 * @param reservas
	 */
	private List<Reserva> obtenerConvocatorias(List<Reserva> reservas) {
		for (Reserva reserva : reservas) {
            if (reserva.getConvocatoria() != null) {
            	reserva.setConvocatoria(reserva.getConvocatoria());
            }
        }
		return reservas;
	}

}
