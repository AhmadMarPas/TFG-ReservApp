package es.ubu.reservapp.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Usuario;
import es.ubu.reservapp.model.repositories.UsuarioRepo;
import java.util.List;
import java.util.regex.Pattern;

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

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2[aby]?\\$\\d{2}\\$[./A-Za-z0-9]{53}");

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
	@Transactional
	public void save(Usuario user) {
        // Password encoding logic: only encode if password is provided and doesn't look like a BCrypt hash
        // This is important for both new users and password changes.
        // If password is not provided during an update, the controller should have set the existing (hashed) password.
        if (user.getPassword() != null && !user.getPassword().isEmpty() && !BCRYPT_PATTERN.matcher(user.getPassword()).matches()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
		usuarioRepo.save(user);
	}

	@Override
	public Usuario findUsuarioById(String id) {
		return usuarioRepo.findById(id).orElse(null);
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
		return usuarioRepo.findByCorreo(correoUsuario).isPresent();
	}

	@Override
	public boolean existeId(String id) {
		if (id == null || id.trim().isEmpty()) {
			return false; 
		}
		return findUsuarioById(id) != null;
	}

	@Override
	public List<Usuario> findAll() {
		return usuarioRepo.findAll();
	}

	@Override
	public Usuario findUsuarioByCorreo(String correo) {
    	return usuarioRepo.findByCorreo(correo).orElse(null);
	}

	@Override
	@Transactional
	public void blockUser(String id) throws UserNotFoundException {
		Usuario usuario = findUsuarioById(id);
		if (usuario != null) {
			usuario.setBloqueado(true);
			usuarioRepo.save(usuario); 
		} else {
			throw new UserNotFoundException("Usuario no encontrado con ID: " + id);
		}
	}

	@Override
	@Transactional
	public void unblockUser(String id) throws UserNotFoundException {
		Usuario usuario = findUsuarioById(id);
		if (usuario != null) {
			usuario.setBloqueado(false);
			usuarioRepo.save(usuario);
		} else {
			throw new UserNotFoundException("Usuario no encontrado con ID: " + id);
		}
	}
}
