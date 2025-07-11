package es.ubu.reservapp.service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import es.ubu.reservapp.exception.UserNotFoundException;
import es.ubu.reservapp.model.entities.Establecimiento;
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
	public void save(Usuario user) {
		// codificación de la contraseña
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
		if (usuario != null && usuario.getPassword() != null && bCryptPasswordEncoder.matches(password, usuario.getPassword())) {
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
	public void unblockUser(String id) throws UserNotFoundException {
		Usuario usuario = findUsuarioById(id);
		if (usuario != null) {
			usuario.setBloqueado(false);
			usuarioRepo.save(usuario);
		} else {
			throw new UserNotFoundException("Usuario no encontrado con ID: " + id);
		}
	}

	@Override
	public void deleteById(String id) throws UserNotFoundException {
        if (findUsuarioById(id) == null) {
            throw new UserNotFoundException("No se pudo eliminar el usuario con ID: " + id);
        }
        usuarioRepo.deleteById(id);
	}
	
    /**
     * Método común para mostrar la asignación de establecimientos.
     * 
     * @param userId ID del usuario
     * @param model Modelo de la vista
     * @return model Modelo con los objetos a mostrar
     */
	@Override
	public Model recuperarEstablecimientosUsuario(String userId, Model model) {
		Usuario usuario = findUsuarioById(userId);
		if (usuario != null) {
			Set<Integer> establecimientosAsignados = usuario.getLstEstablecimientos().stream()
					.map(Establecimiento::getId).collect(Collectors.toSet());

			model.addAttribute("usuario", usuario);
			model.addAttribute("establecimientosAsignados", establecimientosAsignados);
		}
		return model;
	}

	@Override
	public Usuario asignarEstablecimientos(Usuario usuario, List<Establecimiento> establecimientos) {
		usuario = findUsuarioById(usuario.getId());
		if (usuario != null) {
			usuario.getLstEstablecimientos().clear();
			usuario.setLstEstablecimientos(establecimientos);
		}
		return usuario;
	}

	@Override
	public Model obtenerEstablecimientosUsuario(Usuario usuario, Model model) {
		usuario = findUsuarioById(usuario.getId());
		if (usuario != null) {
			List<Establecimiento> establecimientos = usuario.getLstEstablecimientos().stream().toList();

			model.addAttribute("usuario", usuario);
			model.addAttribute("establecimientos", establecimientos);
		}
		return model;
	}

	@Override
	public boolean establecimientoAsignado(Usuario usuario, Establecimiento establecimiento) {
		boolean asignado = false;
		usuario = findUsuarioById(usuario.getId());
		if (usuario != null) {
			List<Establecimiento> establecimientos = usuario.getLstEstablecimientos().stream().toList();
			asignado = establecimientos.contains(establecimiento);
		}
		return asignado;
	}

}
