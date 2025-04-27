package es.ubu.reservapp.service;

import es.ubu.reservapp.model.entities.Usuario;

/**
 * Interface que representa el servicio de la entidad Usuario.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
public interface UsuarioService {

	void save(Usuario user);

	Usuario findUsuarioById(String id);

	Usuario validateAuthentication(String id, String password);

	boolean existeEmail(String correoUsuario);
}
