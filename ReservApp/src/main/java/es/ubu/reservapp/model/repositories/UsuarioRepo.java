package es.ubu.reservapp.model.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.ubu.reservapp.model.entities.Usuario;

/**
 * Repositorio de Usuario.
 * <p>
 * Extiende de JpaRepository<Usuario, String>
 * <p>
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, String> {

	/**
     * Método que busca un usuario por su id y contraseña.
     * 
     * @param id       Id del usuario.
     * @param password Contraseña del usuario.
     * @return Usuario si existe, null en caso contrario.
	 */
	Usuario findUsuarioByIdAndPassword(String id, String password);

	/**
	 * Método que busca un usuario por su id.
	 * 
	 * @param id Id del usuario.
	 * @return Usuario si existe, null en caso contrario.
	 */
	Usuario findUsuarioById(String id);

	/**
	 * Método que busca un usuario por su correo.
	 * 
	 * @param correoUsuario Correo del usuario.
	 * @return Optional<Usuario> si existe, Optional.empty() en caso contrario.
	 */
	Optional<Usuario> findByCorreo(String correoUsuario);

}
