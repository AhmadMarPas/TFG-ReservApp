package es.ubu.reservapp.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Reserva;

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

}
