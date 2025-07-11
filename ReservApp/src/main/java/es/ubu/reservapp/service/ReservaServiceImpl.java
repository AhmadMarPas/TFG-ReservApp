package es.ubu.reservapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Convocatoria;
import es.ubu.reservapp.model.entities.ConvocatoriaPK;
import es.ubu.reservapp.model.entities.Establecimiento;
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

    /**
     * Constructor para la inyección de dependencias.
     * 
     * @param reservaRepo
     * @param usuarioRepo
     * @param convocatoriaService
     */
    public ReservaServiceImpl(ReservaRepo reservaRepo, UsuarioRepo usuarioRepo, ConvocatoriaService convocatoriaService) {
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
        this.convocatoriaService = convocatoriaService;
    }

    @Override
	public Reserva crearReservaConConvocatorias(Reserva reserva, Usuario usuarioQueReserva, List<String> idUsuariosConvocados) throws UserNotFoundException {
        reserva.setUsuario(usuarioQueReserva);
        
        // Guardar la reserva principal primero para obtener su ID generado
        Reserva reservaGuardada = reservaRepo.save(reserva);

        List<Convocatoria> convocatorias = new ArrayList<>();
        if (idUsuariosConvocados != null && !idUsuariosConvocados.isEmpty()) {
            for (String idUsuarioConvocado : idUsuariosConvocados) {
                Optional<Usuario> optUsuarioConvocado = usuarioRepo.findById(idUsuarioConvocado);
                if (optUsuarioConvocado.isPresent()) {
                    Usuario usuarioConvocado = optUsuarioConvocado.get();

                    ConvocatoriaPK convocatoriaPK = new ConvocatoriaPK(reservaGuardada.getId(), usuarioConvocado.getId());
                    Convocatoria convocatoria = new Convocatoria();
                    convocatoria.setId(convocatoriaPK);
                    convocatoria.setReserva(reservaGuardada);
                    convocatoria.setUsuario(usuarioConvocado);
                    
                    convocatoriaService.save(convocatoria); // Guardar a través del servicio de convocatoria
                    convocatorias.add(convocatoria);
                } else {
                	log.error("Usuario convocado con ID " + idUsuarioConvocado + " no encontrado.");
                	throw new UserNotFoundException("El Usuario con ID " + idUsuarioConvocado + " no fue encontrado.");
                }
            }
        }
        reservaGuardada.setConvocatorias(convocatorias);
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
	public Optional<Reserva> findById(Integer id) {
		return reservaRepo.findById(id);
	}

	/**
	 * Recupera las convocatorias de las reserva
	 * 
	 * @param reservas
	 */
	private List<Reserva> obtenerConvocatorias(List<Reserva> reservas) {
		for (Reserva reserva : reservas) {
            if (reserva.getConvocatorias() != null) {
            	reserva.setConvocatorias(new ArrayList<>(reserva.getConvocatorias()));
            	reserva.getConvocatorias().sort(Comparator.comparing(convocatoria -> convocatoria.getUsuario().getNombre()));
            }
        }
		return reservas;
	}

}
