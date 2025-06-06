package es.ubu.reservapp.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	/**
	 * Constructor de la clase.
	 * 
	 * @param usuarioRepo Repositorio de la entidad Usuario.
	 */
	public UsuarioServiceImpl(UsuarioRepo usuarioRepo) {
		this.usuarioRepo = usuarioRepo;
		this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
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
        Usuario usuario = usuarioRepo.findUsuarioById(username);
		if (usuario != null && usuario.getPassword() != null
				&& bCryptPasswordEncoder.matches(password, usuario.getPassword())) {
			return usuario;
		}
		return null;
	}

	@Override
	public boolean existeEmail(String correoUsuario) {
		return usuarioRepo.findByCorreo(correoUsuario) != null;
	}

	@Override
	public boolean existeId(String id) {
		if (id == null || id.trim().isEmpty()) {
			return false; 
		}
		return usuarioRepo.findUsuarioById(id) != null;
	}

}