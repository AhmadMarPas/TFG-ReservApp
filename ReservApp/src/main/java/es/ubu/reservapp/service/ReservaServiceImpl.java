package es.ubu.reservapp.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ReservaServiceImpl(ReservaRepo reservaRepo, UsuarioRepo usuarioRepo, ConvocatoriaService convocatoriaService) {
        this.reservaRepo = reservaRepo;
        this.usuarioRepo = usuarioRepo;
        this.convocatoriaService = convocatoriaService;
    }

    @Override
    @Transactional
	public Reserva crearReservaConConvocatorias(Reserva reserva, List<String> idUsuariosConvocados, String enlaceReunion, 
			String observacionesConvocatoria, Usuario usuarioQueReserva) throws UserNotFoundException {
        reserva.setUsuario(usuarioQueReserva);
        
        // Guardar la reserva principal primero para obtener su ID generado
        Reserva reservaGuardada = reservaRepo.save(reserva);

        Set<Convocatoria> convocatorias = new HashSet<>();
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
                    convocatoria.setEnlace(enlaceReunion);
                    convocatoria.setObservaciones(observacionesConvocatoria);
                    
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
    @Transactional
    public Reserva save(Reserva reserva) {
        return reservaRepo.save(reserva);
    }
    
    @Override
    public List<Reserva> findByUsuario(Usuario usuario) {
        return reservaRepo.findByUsuario(usuario);
    }

	@Override
	public List<Reserva> findByEstablecimientoAndFechaReservaBetween(Establecimiento establecimiento, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
		return reservaRepo.findByEstablecimientoAndFechaReservaBetween(establecimiento, fechaInicio, fechaFin);
	}

	@Override
	public List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaBefore(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual) {
		return reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaBefore(usuario, establecimiento, fechaActual);
	}

	@Override
	public List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual) {
		return reservaRepo.findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(usuario, establecimiento, fechaActual);
	}

	@Override
	public Optional<Reserva> findById(Integer id) {
		return reservaRepo.findById(id);
	}

}
