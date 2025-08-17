package es.ubu.reservapp.model.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Establecimiento;
import es.ubu.reservapp.model.entities.Reserva;
import es.ubu.reservapp.model.entities.Usuario;

/**
 * Repositorio de Reserva.
 * <p>
 * Extiende de JpaRepository<Reserva, Integer>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface ReservaRepo extends JpaRepository<Reserva, Integer> {

	/**
	 * Busca todas las reservas de un usuario para un establecimiento específico que
	 * son anteriores a la fecha actual (reservas pasadas).
	 * 
	 * @param usuario         Usuario que realizó las reservas
	 * @param establecimiento Establecimiento donde se realizaron las reservas
	 * @param fechaActual     Fecha actual para comparar
	 * @return Lista de reservas pasadas
	 */
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaBefore(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);
    
	/**
	 * Busca todas las reservas de un usuario para un establecimiento específico que
	 * son posteriores o iguales a la fecha actual (reservas futuras).
	 * 
	 * @param usuario         Usuario que realizó las reservas
	 * @param establecimiento Establecimiento donde se realizaron las reservas
	 * @param fechaActual     Fecha actual para comparar
	 * @return Lista de reservas futuras
	 */
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);

	/**
	 * Busca todas las reservas para un establecimiento específico que están dentro
	 * de un rango de fechas (inclusive inicio, exclusive fin).
	 * 
	 * @param establecimiento Establecimiento donde se realizaron las reservas
	 * @param fechaInicio     Fecha de inicio del rango (inclusive)
	 * @param fechaFin        Fecha de fin del rango (exclusive)
	 * @return Lista de reservas dentro del rango de fechas
	 */
    List<Reserva> findByEstablecimientoAndFechaReservaBetween(Establecimiento establecimiento, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca las reservas de un usuario.
     * 
     * @param usuario Usuario al que se quiere consultar las reservas.
     * @return Lista de reservas realizadas por el usuario.
     */
    List<Reserva> findByUsuario(Usuario usuario);

    /**
     * Busca reservas que se solapen con un horario específico en un establecimiento y fecha determinada.
     * Una reserva se solapa si:
     * - Su hora de inicio es anterior a la hora de fin del nuevo horario Y
     * - Su hora de fin es posterior a la hora de inicio del nuevo horario
     * 
     * @param establecimiento Establecimiento donde buscar las reservas
     * @param fechaHoraInicio Fecha y hora de inicio del horario a verificar
     * @param horaFin Hora de fin del horario a verificar
     * @return Lista de reservas que se solapan con el horario especificado
     */
    @Query("SELECT r FROM Reserva r WHERE r.establecimiento = :establecimiento " +
    	       "AND DATE(r.fechaReserva) = DATE(:fechaHoraInicio) " +
    	       "AND r.fechaReserva < :fechaHoraFin " +
    	       "AND (r.horaFin IS NULL OR " +
    	       "     TIMESTAMP(DATE(r.fechaReserva), r.horaFin) > :fechaHoraInicio)")
   	List<Reserva> findReservasSolapadas(@Param("establecimiento") Establecimiento establecimiento,
   	                                   @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
   	                                   @Param("fechaHoraFin") LocalDateTime fechaHoraFin);
    /**
     * Cuenta el número de reservas que se solapan con un horario específico en un establecimiento y fecha determinada.
     * 
     * @param establecimiento Establecimiento donde contar las reservas
     * @param fechaHoraInicio Inicio y hora de inicio del horario a verificar
     * @param fechaHoraFin y hora de Fin del horario a verificar
     * @return Número de reservas que se solapan con el horario especificado
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.establecimiento = :establecimiento " +
    	       "AND DATE(r.fechaReserva) = DATE(:fechaHoraInicio) " +
    	       "AND r.fechaReserva < :fechaHoraFin " +
    	       "AND (r.horaFin IS NULL OR " +
    	       "     TIMESTAMP(DATE(r.fechaReserva), r.horaFin) > :fechaHoraInicio)")
   	Long countReservasSolapadas(@Param("establecimiento") Establecimiento establecimiento,
   	                           @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
   	                           @Param("fechaHoraFin") LocalDateTime fechaHoraFin);

    /**
     * Busca todas las reservas de un establecimiento en una fecha específica.
     * 
     * @param establecimiento Establecimiento donde buscar las reservas
     * @param fechaInicio Inicio del día (00:00:00)
     * @param fechaFin Fin del día (23:59:59)
     * @return Lista de reservas del establecimiento en la fecha especificada
     */
    @Query("SELECT r FROM Reserva r WHERE r.establecimiento = :establecimiento " +
           "AND r.fechaReserva >= :fechaInicio AND r.fechaReserva <= :fechaFin " +
           "ORDER BY r.fechaReserva ASC")
    List<Reserva> findReservasByEstablecimientoAndFecha(@Param("establecimiento") Establecimiento establecimiento,
                                                       @Param("fechaInicio") LocalDateTime fechaInicio,
                                                       @Param("fechaFin") LocalDateTime fechaFin);
}