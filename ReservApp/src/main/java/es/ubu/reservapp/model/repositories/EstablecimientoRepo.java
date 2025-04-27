package es.ubu.reservapp.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Establecimiento;

/**
 * Repositorio de Establecimiento.
 * <p>
 * Extiende de JpaRepository<Establecimiento, Integer>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface EstablecimientoRepo extends JpaRepository<Establecimiento, Integer> {

}
