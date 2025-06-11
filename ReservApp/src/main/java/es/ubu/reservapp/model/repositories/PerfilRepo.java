package es.ubu.reservapp.model.repositories;

import java.util.Optional;

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

	/**
	 * Busca un perfil por su nombre.
	 * 
	 * @param nombre el nombre del perfil a buscar.
	 * @return un Optional que contiene el perfil si se encuentra, o vacío si no.
	 */
	Optional<Perfil> findByNombre(String nombre);

}
