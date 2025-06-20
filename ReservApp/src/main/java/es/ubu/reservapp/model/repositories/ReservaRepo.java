package es.ubu.reservapp.model.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * Busca todas las reservas de un usuario para un establecimiento específico
     * que son anteriores a la fecha actual (reservas pasadas).
     * 
     * @param usuario Usuario que realizó las reservas
     * @param establecimiento Establecimiento donde se realizaron las reservas
     * @param fechaActual Fecha actual para comparar
     * @return Lista de reservas pasadas
     */
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaBefore(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);
    
    /**
     * Busca todas las reservas de un usuario para un establecimiento específico
     * que son posteriores o iguales a la fecha actual (reservas futuras).
     * 
     * @param usuario Usuario que realizó las reservas
     * @param establecimiento Establecimiento donde se realizaron las reservas
     * @param fechaActual Fecha actual para comparar
     * @return Lista de reservas futuras
     */
    List<Reserva> findByUsuarioAndEstablecimientoAndFechaReservaGreaterThanEqual(Usuario usuario, Establecimiento establecimiento, LocalDateTime fechaActual);

}