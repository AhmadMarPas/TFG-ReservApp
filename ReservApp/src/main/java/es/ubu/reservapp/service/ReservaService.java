package es.ubu.reservapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;

/**
 * Interfaz que representa el servicio de la entidad Reserva.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface ReservaService {
	
	/**
	 * Crea la convocatoria de la reserva con los usuarios indicados.
	 * 
	 * @param reserva
	 * @param idUsuariosConvocados
	 * @param usuarioQueReserva
	 * @return La reserva creara.
	 */
    @Transactional
    Reserva crearReservaConConvocatorias(Reserva reserva, Usuario usuarioQueReserva, List<String> idUsuariosConvocados) throws UserNotFoundException;

    /**
     * Recupera las reservas de un establecimiento entre dos fechas.
     * 
     * @param establecimiento Establecimiento para el que se quiere consultar las reservas.
     * @param fechaInicio Fecha de inicio del rango de búsqueda.
     * @param fechaFin Fecha de fin del rango de búsqueda.
     * @return Lista de resrvas dentro del rango de fechas para el establecimiento indicado.
     */
    @Transactional(readOnly = true)
    List<Reserva> findByEstablecimientoAndFechaReservaBetween(Establecimiento establecimiento, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
	/**
	 * Busca todas las reservas de un usuario para un establecimiento específico que
	 * son posteriores o iguales a la fecha actual (reservas futuras).
	 * 
	 * @param usuario         Usuario que realizó las reservas
	 * @param establecimiento Establecimiento donde se realizaron las reservas
	 * @param fechaActual     Fecha actual para comparar
	 * @return Lista de reservas futuras
	 */
    @Transactional(readOnly = true)
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);

	/**
	 * Busca todas las reservas de un usuario para un establecimiento específico que
	 * son anteriores a la fecha actual (reservas pasadas).
	 * 
	 * @param usuario         Usuario que realizó las reservas
	 * @param establecimiento Establecimiento donde se realizaron las reservas
	 * @param fechaActual     Fecha actual para comparar
	 * @return Lista de reservas pasadas
	 */
    @Transactional(readOnly = true)
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaBefore(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);

    /**
     * Recupera todas las reservas.
     * 
     * @return Lista con todas las reservas.
     */
    @Transactional(readOnly = true)
    List<Reserva> findAll();

    /**
     * Guarda la reserva.
     * 
     * @param reserva a guardar.
     * @return Reserva guardada.
     */
    @Transactional
    Reserva save(Reserva reserva);
    
    /**
     * Busca las reservas del usuario.
     * 
     * @param usuario Usuario al que se quiere consultar las reservas.
     * @return Lista de reservas realizadas por el usuario.
     */
    @Transactional(readOnly = true)
    List<Reserva> findByUsuario(Usuario usuario);

    /**
     * Busca una reserva por su ID.
     * 
     * @param id ID de la reserva a buscar.
     * @return Optional con la reserva si existe, vacío si no.
     */
    @Transactional(readOnly = true)
    Optional<Reserva> findById(Integer id);

}
