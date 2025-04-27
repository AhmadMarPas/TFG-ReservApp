package es.ubu.reservapp.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Menu;

/**
 * Repositorio de Menu.
 * <p>
 * Extiende de JpaRepository<Menu, Integer>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface MenuRepo extends JpaRepository<Menu, Integer> {

}
