package es.ubu.reservapp.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Perfil;

/**
 * Repositorio de Perfil.
 * <p>
 * Extiende de JpaRepository<Perfil, Integer>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface PerfilRepo extends JpaRepository<Perfil, Integer> {

}
