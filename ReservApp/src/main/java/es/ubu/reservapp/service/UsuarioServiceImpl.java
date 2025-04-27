package es.ubu.reservapp.service;

import org.springframework.stereotype.Service;

import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;

/**
 * Clase que implementa el servicio de la entidad Usuario.
 * 
 * @autor Ahmad Mareie Pascual
 * @version 1.0
 * @since 1.0
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

	/**
	 * Repositorio de la entidad Usuario.
	 */
	private final UsuarioRepo usuarioRepo;

	/**
	 * Constructor de la clase.
	 * 
	 * @param usuarioRepo Repositorio de la entidad Usuario.
	 */
	public UsuarioServiceImpl(UsuarioRepo usuarioRepo) {
		this.usuarioRepo = usuarioRepo;
	}

	@Override
	public void save(Usuario user) {
		usuarioRepo.save(user);
	}

	@Override
	public Usuario findUsuarioById(String id) {
		return usuarioRepo.findUsuarioById(id);
	}

	@Override
	public Usuario validateAuthentication(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        return usuarioRepo.findUsuarioByIdAndPassword(username, password);
    }

	@Override
	public boolean existeEmail(String correoUsuario) {
		return usuarioRepo.findByCorreo(correoUsuario) != null;
	}

}
